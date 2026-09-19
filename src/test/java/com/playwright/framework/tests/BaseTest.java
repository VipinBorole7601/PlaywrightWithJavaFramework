package com.playwright.framework.tests;

import com.microsoft.playwright.Page;
import com.playwright.framework.config.ConfigManager;
import com.playwright.framework.config.FrameworkConfig;
import com.playwright.framework.driver.PlaywrightManager;
import com.playwright.framework.driver.SessionOptions;
import com.playwright.framework.pages.HomePage;
import com.playwright.framework.reports.ExtentReportExtension;
import com.playwright.framework.utils.ScreenshotUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Listeners;

import java.nio.file.Path;

@Listeners({ExtentReportExtension.class, QuarantineListener.class})
public abstract class BaseTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseTest.class);
    protected HomePage homePage;

    protected void setUpForBrowser(String browserName) {
        FrameworkConfig config = ConfigManager.load();
        PlaywrightManager.initialize(SessionOptions.defaults()
                .withBrowser(browserName)
                .withVideoRecording(config.videoOnFailure())
                .withTracing(config.traceOnFailure()));
        PlaywrightManager.page().navigate(config.baseUrl());
        homePage = new HomePage(PlaywrightManager.page());
    }

    @AfterMethod(alwaysRun = true)
    void tearDown(ITestResult result) {
        boolean failed = result.getStatus() == ITestResult.FAILURE;
        String label = result.getName() + "-" + safe(PlaywrightManager.activeBrowser());

        try {
            if (failed && currentPage() != null) {
                ScreenshotUtil.capture(currentPage(), label);
                LOGGER.error("Test failed: {}", result.getName(), result.getThrowable());
                Path trace = PlaywrightManager.saveTrace(label);
                if (trace != null) {
                    LOGGER.error("Failure trace: {}", trace);
                }
            } else {
                PlaywrightManager.discardTrace();
            }
        } finally {
            PlaywrightManager.shutdown(failed);
        }
    }

    protected Page currentPage() {
        try {
            return PlaywrightManager.page();
        } catch (IllegalStateException exception) {
            return null;
        }
    }

    private static String safe(String value) {
        return value == null ? "unknown" : value;
    }
}

