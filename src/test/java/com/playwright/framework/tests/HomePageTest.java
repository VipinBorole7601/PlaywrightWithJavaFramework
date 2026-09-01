package com.playwright.framework.tests;

import com.playwright.framework.reports.ExtentReportExtension;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

@Listeners(ExtentReportExtension.class)
public class HomePageTest extends BaseTest {

    @DataProvider(name = "browsers")
    public Object[][] browsers() {
        return new Object[][] {
                {"chromium"},
                {"firefox"},
                {"webkit"}
        };
    }

    @Test(dataProvider = "browsers", groups = "smoke", retryAnalyzer = NetworkRetryAnalyzer.class)
    public void shouldOpenPlaywrightHomePage(String browser) {
        setUpForBrowser(browser);
        assertTrue(homePage.title().contains("Playwright"), "Unexpected page title");
        assertTrue(homePage.isDocsLinkVisible(), "Expected docs link to be visible");
    }

    @Test(dataProvider = "browsers", groups = "regression", retryAnalyzer = NetworkRetryAnalyzer.class)
    public void shouldLoadTheHomePageWithoutErrors(String browser) {
        setUpForBrowser(browser);
        assertTrue(homePage.title().length() > 0, "Page title should not be empty");
    }
}
