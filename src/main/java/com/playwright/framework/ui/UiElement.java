package com.playwright.framework.ui;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.options.LoadState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Fluent wrapper around a Playwright {@link Locator}. Adds descriptive logging,
 * Allure step reporting, chained waits, and assertion helpers so tests and page
 * objects don't repeat boilerplate.
 */
public final class UiElement {
    private static final Logger LOGGER = LoggerFactory.getLogger(UiElement.class);

    private final Locator locator;
    private final String description;

    private UiElement(Locator locator, String description) {
        this.locator = locator;
        this.description = description == null || description.isBlank() ? "element" : description;
    }

    public static UiElement of(Locator locator, String description) {
        return new UiElement(locator, description);
    }

    public Locator raw() {
        return locator;
    }

    public String description() {
        return description;
    }

    /* -------------------- Actions -------------------- */

    public UiElement click() {
        return step("click " + description, locator::click);
    }

    public UiElement doubleClick() {
        return step("double click " + description, locator::dblclick);
    }

    public UiElement type(String value) {
        return step("type '" + value + "' into " + description, () -> locator.fill(value));
    }

    public UiElement press(String key) {
        return step("press '" + key + "' on " + description, () -> locator.press(key));
    }

    public UiElement clear() {
        return step("clear " + description, () -> locator.fill(""));
    }

    public UiElement hover() {
        return step("hover over " + description, locator::hover);
    }

    public UiElement scrollIntoView() {
        return step("scroll " + description + " into view", locator::scrollIntoViewIfNeeded);
    }

    /* -------------------- Waits -------------------- */

    public UiElement waitUntilVisible() {
        return step("wait until " + description + " is visible",
                () -> locator.waitFor(new Locator.WaitForOptions()
                        .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)));
    }

    public UiElement waitUntilHidden() {
        return step("wait until " + description + " is hidden",
                () -> locator.waitFor(new Locator.WaitForOptions()
                        .setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN)));
    }

    public UiElement waitUntilAttached() {
        return step("wait until " + description + " is attached",
                () -> locator.waitFor(new Locator.WaitForOptions()
                        .setState(com.microsoft.playwright.options.WaitForSelectorState.ATTACHED)));
    }

    /* -------------------- Queries -------------------- */

    public String text() {
        String value = locator.textContent();
        return value == null ? "" : value.trim();
    }

    public String value() {
        return locator.inputValue();
    }

    public boolean isVisible() {
        return locator.isVisible();
    }

    public boolean isEnabled() {
        return locator.isEnabled();
    }

    public int count() {
        return locator.count();
    }

    public UiElement nth(int index) {
        return new UiElement(locator.nth(index), description + "[" + index + "]");
    }

    public UiElement child(String selector, String childDescription) {
        return new UiElement(locator.locator(selector), description + " > " + childDescription);
    }

    /* -------------------- Assertions -------------------- */

    public UiElement shouldBeVisible() {
        return assertion("expect " + description + " to be visible", assertThat(locator)::isVisible);
    }

    public UiElement shouldBeHidden() {
        return assertion("expect " + description + " to be hidden", assertThat(locator)::isHidden);
    }

    public UiElement shouldBeEnabled() {
        return assertion("expect " + description + " to be enabled", assertThat(locator)::isEnabled);
    }

    public UiElement shouldHaveText(String expected) {
        return assertion("expect " + description + " to have text '" + expected + "'",
                () -> assertThat(locator).hasText(expected));
    }

    public UiElement shouldContainText(String expected) {
        return assertion("expect " + description + " to contain text '" + expected + "'",
                () -> assertThat(locator).containsText(expected));
    }

    public UiElement shouldMatchText(Pattern pattern) {
        return assertion("expect " + description + " to match /" + pattern.pattern() + "/",
                () -> assertThat(locator).hasText(pattern));
    }

    public UiElement shouldHaveCount(int expected) {
        return assertion("expect " + description + " to have count " + expected,
                () -> assertThat(locator).hasCount(expected));
    }

    public UiElement shouldHaveAttribute(String name, String value) {
        return assertion("expect " + description + "[" + name + "] = '" + value + "'",
                () -> assertThat(locator).hasAttribute(name, value));
    }

    /* -------------------- Internal helpers -------------------- */

    private UiElement step(String label, Runnable action) {
        LOGGER.info("[step] {}", label);
        action.run();
        return this;
    }

    private UiElement assertion(String label, Runnable check) {
        LOGGER.info("[assert] {}", label);
        check.run();
        return this;
    }

    LocatorAssertions expect() {
        return assertThat(locator);
    }

    public UiElement waitForPageLoad(com.microsoft.playwright.Page page) {
        return step("wait for page load state = LOAD", () -> page.waitForLoadState(LoadState.LOAD));
    }
}
