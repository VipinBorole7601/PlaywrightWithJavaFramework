package com.playwright.framework.reports;

import com.aventstack.extentreports.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class ExtentReportExtension implements ITestListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtentReportExtension.class);

    @Override
    public void onStart(ITestContext context) {
        LOGGER.info("Initializing Extent Reports for test class: {}", context.getCurrentXmlTest().getName());
        ExtentManager.getInstance();
    }

    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getTestClass().getName() + " :: " + result.getName();
        ExtentTestManager.setTest(ExtentManager.getInstance().createTest(testName));
        LOGGER.info("Started test: {}", testName);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTestManager.getTest().log(Status.PASS, "Test passed successfully");
        LOGGER.info("Test passed: {}", result.getName());
        ExtentTestManager.unload();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        Throwable exception = result.getThrowable();
        ExtentTestManager.getTest().log(Status.FAIL, exception != null ? exception.getMessage() : "Test failed");
        if (exception != null) {
            ExtentTestManager.getTest().log(Status.FAIL, exception);
        }
        LOGGER.error("Test failed: {}", result.getName(), exception);
        ExtentTestManager.unload();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTestManager.getTest().log(Status.SKIP, "Test skipped");
        ExtentTestManager.unload();
    }

    @Override
    public void onFinish(ITestContext context) {
        LOGGER.info("Flushing Extent Reports");
        ExtentManager.flush();
    }
}
