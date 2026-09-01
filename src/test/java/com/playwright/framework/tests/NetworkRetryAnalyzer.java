package com.playwright.framework.tests;

import com.microsoft.playwright.PlaywrightException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Retries tests that fail due to transient network errors (DNS resolution failures,
 * network changes, etc.) rather than assertion failures.
 */
public class NetworkRetryAnalyzer implements IRetryAnalyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkRetryAnalyzer.class);
    private static final int MAX_RETRIES = 2;

    private int retryCount = 0;

    @Override
    public boolean retry(ITestResult result) {
        if (retryCount >= MAX_RETRIES) {
            return false;
        }
        Throwable cause = result.getThrowable();
        if (isNetworkError(cause)) {
            retryCount++;
            LOGGER.warn("Network error on attempt {}/{} for test '{}': {}",
                    retryCount, MAX_RETRIES, result.getName(), cause.getMessage());
            return true;
        }
        return false;
    }

    private boolean isNetworkError(Throwable throwable) {
        if (throwable == null) {
            return false;
        }
        if (throwable instanceof PlaywrightException) {
            String message = throwable.getMessage();
            if (message != null && (
                    message.contains("ERR_NAME_NOT_RESOLVED") ||
                    message.contains("ERR_NETWORK_CHANGED") ||
                    message.contains("ERR_INTERNET_DISCONNECTED") ||
                    message.contains("NS_ERROR_UNKNOWN_HOST") ||
                    message.contains("Could not resolve hostname") ||
                    message.contains("net::ERR_"))) {
                return true;
            }
        }
        return isNetworkError(throwable.getCause());
    }
}
