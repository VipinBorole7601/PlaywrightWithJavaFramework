package com.playwright.framework.tests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

import java.lang.reflect.Method;

/**
 * Quarantine handling for tests annotated with {@link Flaky}.
 *
 * <p>Behaviour:
 * <ul>
 *   <li>Always logs a WARN when a flaky test runs so the ticket ID is visible.</li>
 *   <li>When {@code -Dquarantine.mode=true} (or env {@code QUARANTINE_MODE=true}) and a
 *       {@code @Flaky} test fails, the result is downgraded from FAILURE to SKIP so
 *       the pipeline stays green while the underlying issue is being fixed.</li>
 *   <li>Non-flaky tests are never affected.</li>
 * </ul>
 *
 * Implemented via {@link IInvokedMethodListener} so status mutation happens
 * before TestNG / Surefire route the result to their failure counters.
 */
public class QuarantineListener implements IInvokedMethodListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuarantineListener.class);

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult result) {
        if (!method.isTestMethod()) {
            return;
        }
        Flaky flaky = flakyOf(result);
        if (flaky != null) {
            LOGGER.warn("[flaky] Running quarantined test '{}' (ticket: {})",
                    result.getName(), flaky.value());
        }
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult result) {
        if (!method.isTestMethod()) {
            return;
        }
        if (result.getStatus() != ITestResult.FAILURE) {
            return;
        }
        if (!quarantineMode()) {
            return;
        }
        Flaky flaky = flakyOf(result);
        if (flaky == null) {
            return;
        }
        LOGGER.warn("[quarantine] Flaky test '{}' (ticket: {}) failed but quarantine.mode=true — marking SKIP",
                result.getName(), flaky.value());
        Throwable original = result.getThrowable();
        result.setStatus(ITestResult.SKIP);
        result.setThrowable(new org.testng.SkipException(
                "Quarantined flaky test '" + result.getName() + "' — ticket " + flaky.value()
                        + " — original cause: " + (original == null ? "n/a" : original.toString())));
    }

    private static Flaky flakyOf(ITestResult result) {
        Method method = result.getMethod().getConstructorOrMethod().getMethod();
        return method == null ? null : method.getAnnotation(Flaky.class);
    }

    private static boolean quarantineMode() {
        String raw = System.getProperty("quarantine.mode");
        if (raw == null || raw.isBlank()) {
            raw = System.getenv("QUARANTINE_MODE");
        }
        return raw != null && Boolean.parseBoolean(raw.trim());
    }
}

