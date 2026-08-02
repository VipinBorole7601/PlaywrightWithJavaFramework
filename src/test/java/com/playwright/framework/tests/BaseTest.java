package com.playwright.framework.tests;

import com.microsoft.playwright.Page;
import com.playwright.framework.config.ConfigManager;
import com.playwright.framework.config.FrameworkConfig;
import com.playwright.framework.driver.PlaywrightManager;
import com.playwright.framework.pages.HomePage;
import com.playwright.framework.utils.ScreenshotUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.extension.TestWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseTest.class);
    protected HomePage homePage;

    @RegisterExtension
    TestWatcher screenshotOnFailure = new TestWatcher() {
        @Override
        public void testFailed(ExtensionContext context, Throwable cause) {
            if (currentPage() != null) {
                ScreenshotUtil.capture(currentPage(), context.getDisplayName());
            }
            LOGGER.error("Test failed: {}", context.getDisplayName(), cause);
        }
    };

    @BeforeEach
    void setUp() {
        FrameworkConfig config = ConfigManager.load();
        PlaywrightManager.initialize();
        PlaywrightManager.page().navigate(config.baseUrl());
        homePage = new HomePage(PlaywrightManager.page());
    }

    @AfterEach
    void tearDown() {
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
