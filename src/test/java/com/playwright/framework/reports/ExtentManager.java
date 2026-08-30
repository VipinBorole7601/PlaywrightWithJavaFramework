package com.playwright.framework.reports;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class ExtentManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtentManager.class);
    private static ExtentReports extentReports;

    private ExtentManager() {}

    public static synchronized ExtentReports getInstance() {
        if (extentReports == null) {
            Path outputDir = Paths.get("test-output");
            try {
                Files.createDirectories(outputDir);
            } catch (Exception e) {
                LOGGER.error("Unable to create test-output directory", e);
                throw new RuntimeException("Unable to create test-output directory", e);
            }

            ExtentSparkReporter spark = new ExtentSparkReporter(outputDir.resolve("ExtentReport.html").toString());
            spark.config().setReportName("Playwright Java Automation Report");
            spark.config().setDocumentTitle("Test Execution Report");
            spark.config().setTheme(com.aventstack.extentreports.reporter.configuration.Theme.DARK);

            extentReports = new ExtentReports();
            extentReports.attachReporter(spark);
            extentReports.setSystemInfo("Framework", "Playwright Java");
            extentReports.setSystemInfo("Browser", System.getProperty("browser", "chromium"));
            extentReports.setSystemInfo("Headless", System.getProperty("headless", "true"));
            extentReports.setSystemInfo("Base URL", System.getProperty("baseUrl", "N/A"));
            
            LOGGER.info("Extent Reports initialized at {}", outputDir.resolve("ExtentReport.html"));
        }
        return extentReports;
    }

    public static synchronized void flush() {
        if (extentReports != null) {
            extentReports.flush();
            LOGGER.info("Extent Reports flushed successfully");
        }
    }
}
