package com.playwright.framework.tests;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HomePageTest extends BaseTest {

    @Test
    @Tag("smoke")
    void shouldOpenPlaywrightHomePage() {
        assertTrue(homePage.title().contains("Playwright"), "Unexpected page title");
        assertTrue(homePage.isDocsLinkVisible(), "Expected docs link to be visible");
    }

    @Test
    @Tag("regression")
    void shouldLoadTheHomePageWithoutErrors() {
        assertTrue(homePage.title().length() > 0, "Page title should not be empty");
    }
}
