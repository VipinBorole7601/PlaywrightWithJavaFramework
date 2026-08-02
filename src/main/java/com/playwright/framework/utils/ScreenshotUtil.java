package com.playwright.framework.utils;

import com.microsoft.playwright.Page;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ScreenshotUtil {
    private static final Path SCREENSHOT_DIR = Path.of("test-results", "screenshots");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private ScreenshotUtil() {
    }

    public static Path capture(Page page, String testName) {
        try {
            Files.createDirectories(SCREENSHOT_DIR);
            String fileName = sanitize(testName) + "-" + LocalDateTime.now().format(FORMATTER) + ".png";
            Path output = SCREENSHOT_DIR.resolve(fileName);
            page.screenshot(new Page.ScreenshotOptions().setPath(output).setFullPage(true));
            return output;
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to capture screenshot", exception);
        }
    }

    private static String sanitize(String value) {
        return value.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
