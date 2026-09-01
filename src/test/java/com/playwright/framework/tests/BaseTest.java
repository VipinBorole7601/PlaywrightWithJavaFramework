package com.playwright.framework.tests;

import com.microsoft.playwright.Page;
import com.playwright.framework.config.ConfigManager;
import com.playwright.framework.config.FrameworkConfig;
import com.playwright.framework.driver.PlaywrightManager;
import com.playwright.framework.pages.HomePage;
import com.playwright.framework.reports.ExtentReportExtension;
import com.playwright.framework.utils.ScreenshotUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Listeners;

@Listeners(ExtentReportExtension.class)
public abstract class BaseTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseTest.class);
    protected HomePage homePage;

    protected void setUpForBrowser(String browserName) {
        System.setProperty("browser", browserName);
        FrameworkConfig config = ConfigManager.load();
        PlaywrightManager.initialize();
        PlaywrightManager.page().navigate(config.baseUrl());
        homePage = new HomePage(PlaywrightManager.page());
    }

    @AfterMethod(alwaysRun = true)
    void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE && currentPage() != null) {
            ScreenshotUtil.capture(currentPage(), result.getName());
            LOGGER.error("Test failed: {}", result.getName(), result.getThrowable());
        }
        PlaywrightManager.shutdown();
    }

    protected Page currentPage() {
        try {
            return PlaywrightManager.page();
        } catch (IllegalStateException exception) {
            return null;
        }
    }
}
