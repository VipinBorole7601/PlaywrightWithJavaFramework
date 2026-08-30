package com.playwright.framework.reports;

import com.aventstack.extentreports.Status;
import org.junit.jupiter.api.extension.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtentReportExtension implements BeforeAllCallback, BeforeEachCallback, 
        AfterTestExecutionCallback, AfterEachCallback, AfterAllCallback {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtentReportExtension.class);

    @Override
    public void beforeAll(ExtensionContext context) {
        LOGGER.info("Initializing Extent Reports for test class: {}", context.getRequiredTestClass().getSimpleName());
        ExtentManager.getInstance();
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        String testName = context.getRequiredTestClass().getSimpleName() + " :: " + context.getDisplayName();
        ExtentTestManager.setTest(ExtentManager.getInstance().createTest(testName));
        LOGGER.info("Started test: {}", testName);
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        if (context.getExecutionException().isEmpty()) {
            ExtentTestManager.getTest().log(Status.PASS, "Test passed successfully");
        }
    }

    @Override
    public void afterEach(ExtensionContext context) {
        if (context.getExecutionException().isPresent()) {
            Throwable exception = context.getExecutionException().get();
            ExtentTestManager.getTest().log(Status.FAIL, exception.getMessage());
            ExtentTestManager.getTest().log(Status.FAIL, exception);
            LOGGER.error("Test failed: {}", context.getDisplayName(), exception);
        } else {
            LOGGER.info("Test passed: {}", context.getDisplayName());
        }
        ExtentTestManager.unload();
    }

    @Override
    public void afterAll(ExtensionContext context) {
        LOGGER.info("Flushing Extent Reports");
        ExtentManager.flush();
    }
}
