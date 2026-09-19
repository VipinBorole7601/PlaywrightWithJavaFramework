package com.playwright.framework.config;

public record FrameworkConfig(
        String baseUrl,
        String browser,
        boolean headless,
        int timeoutMs,
        int slowMo,
        String env,
        boolean traceOnFailure,
        boolean videoOnFailure) {
}

