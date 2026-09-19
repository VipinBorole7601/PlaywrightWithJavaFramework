package com.playwright.framework.tests;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a test as a known-flaky test tracked by a ticket. The framework gives
 * these tests extra retries and — when {@code -Dquarantine.mode=true} — converts
 * their failures into skips so they don't break the pipeline while being fixed.
 *
 * Use sparingly. Always link a ticket in {@link #value()}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Flaky {
    /** Ticket ID / short reason. e.g. {@code "JIRA-1234"} or {@code "flaky-search-suggest"}. */
    String value();

    /** Max retry attempts inside a single test run. Defaults to 3. */
    int maxRetries() default 3;
}
