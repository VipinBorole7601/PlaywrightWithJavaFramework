package com.playwright.framework.tests;

import com.playwright.framework.reports.ExtentReportExtension;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.testng.Assert.assertTrue;

@Listeners(ExtentReportExtension.class)
public class HomePageTest extends BaseTest {

    private static final List<String> ALL_BROWSERS = List.of("chromium", "firefox", "webkit");

    @DataProvider(name = "browsers", parallel = false)
    public Object[][] browsers() {
        String requested = System.getProperty("browsers", "");
        Stream<String> source = requested.isBlank()
                ? ALL_BROWSERS.stream()
                : Arrays.stream(requested.split(",")).map(String::trim).filter(s -> !s.isEmpty());
        return source
                .filter(ALL_BROWSERS::contains)
                .map(browser -> new Object[]{browser})
                .toArray(Object[][]::new);
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
