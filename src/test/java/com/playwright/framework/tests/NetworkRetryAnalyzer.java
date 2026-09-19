package com.playwright.framework.tests;

import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.TimeoutError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Set;

/**
 * Retries tests only when the underlying failure looks transient (network blip,
 * DNS glitch, Playwright timeout on a still-loading resource, browser/context
 * dropped). Assertion failures, NPEs, and configuration errors are never retried.
 *
 * Max attempts:
 *   - {@link Flaky#maxRetries()} if the test is annotated with {@link Flaky}
 *   - otherwise value of config key {@code retry.max} (default 2)
 */
public class NetworkRetryAnalyzer implements IRetryAnalyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkRetryAnalyzer.class);

    private static final Set<String> PLAYWRIGHT_NETWORK_SIGNATURES = Set.of(
            "ERR_NAME_NOT_RESOLVED",
            "ERR_NETWORK_CHANGED",
            "ERR_INTERNET_DISCONNECTED",
            "ERR_CONNECTION_RESET",
            "ERR_CONNECTION_REFUSED",
            "ERR_CONNECTION_CLOSED",
            "ERR_TIMED_OUT",
            "NS_ERROR_UNKNOWN_HOST",
            "NS_ERROR_NET_RESET",
            "NS_ERROR_NET_INTERRUPT",
            "Could not resolve hostname",
            "net::ERR_"
    );

    private static final Set<String> PLAYWRIGHT_TRANSIENT_SIGNATURES = Set.of(
            "Target page, context or browser has been closed",
            "Browser has been closed",
            "Page closed",
            "Navigation failed because page was closed",
            "Timeout ",
            "waiting for"
    );

    private int retryCount = 0;

    @Override
    public boolean retry(ITestResult result) {
        int max = maxRetries(result);
        if (retryCount >= max) {
            return false;
        }
        Throwable cause = result.getThrowable();
        if (!isRetryable(cause)) {
            return false;
        }
        retryCount++;
        LOGGER.warn("Retrying '{}' — attempt {}/{} — cause: {}",
                result.getName(), retryCount, max, rootMessage(cause));
        return true;
    }

    private static int maxRetries(ITestResult result) {
        Flaky flaky = flakyOf(result);
        if (flaky != null) {
            return Math.max(0, flaky.maxRetries());
        }
        String raw = System.getProperty("retry.max");
        if (raw == null || raw.isBlank()) {
            raw = System.getenv("RETRY_MAX");
        }
        if (raw == null || raw.isBlank()) {
            return 2;
        }
        try {
            return Math.max(0, Integer.parseInt(raw.trim()));
        } catch (NumberFormatException ignored) {
            return 2;
        }
    }

    static Flaky flakyOf(ITestResult result) {
        Method method = result.getMethod().getConstructorOrMethod().getMethod();
        return method == null ? null : method.getAnnotation(Flaky.class);
    }

    private static boolean isRetryable(Throwable throwable) {
        if (throwable == null) {
            return false;
        }
        // Never retry programming or assertion errors.
        if (throwable instanceof AssertionError
                || throwable instanceof NullPointerException
                || throwable instanceof IllegalArgumentException
                || throwable instanceof IllegalStateException) {
            return false;
        }
        if (throwable instanceof TimeoutError) {
            return true;
        }
        if (throwable instanceof UnknownHostException
                || throwable instanceof ConnectException
                || throwable instanceof SocketTimeoutException
                || throwable instanceof SocketException
                || throwable instanceof IOException) {
            return true;
        }
        if (throwable instanceof PlaywrightException) {
            String message = throwable.getMessage() == null ? "" : throwable.getMessage();
            if (containsAny(message, PLAYWRIGHT_NETWORK_SIGNATURES)
                    || containsAny(message, PLAYWRIGHT_TRANSIENT_SIGNATURES)) {
                return true;
            }
        }
        return isRetryable(throwable.getCause());
    }

    private static boolean containsAny(String haystack, Set<String> needles) {
        for (String needle : needles) {
            if (haystack.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private static String rootMessage(Throwable throwable) {
        Throwable cursor = throwable;
        while (cursor.getCause() != null && cursor.getCause() != cursor) {
            cursor = cursor.getCause();
        }
        return cursor.getClass().getSimpleName() + ": " + cursor.getMessage();
    }
}

