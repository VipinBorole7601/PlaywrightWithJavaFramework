package com.playwright.framework.ui;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.util.regex.Pattern;

/**
 * Page-level wait helpers. Prefer these over Thread.sleep or ad-hoc polling.
 * All methods rely on Playwright's built-in auto-waiting where possible.
 */
public final class Waits {

    private Waits() {
    }

    public static void forLoad(Page page) {
        page.waitForLoadState(LoadState.LOAD);
    }

    public static void forDomContentLoaded(Page page) {
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
    }

    public static void forNetworkIdle(Page page) {
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    public static void forUrlContains(Page page, String fragment) {
        page.waitForURL(url -> url != null && url.contains(fragment));
    }

    public static void forUrlMatches(Page page, Pattern pattern) {
        page.waitForURL(pattern);
    }

    public static void forTitleContains(Page page, String fragment) {
        page.waitForFunction("expected => document.title.includes(expected)", fragment);
    }

    public static void forSelectorVisible(Page page, String selector) {
        page.locator(selector).waitFor(new com.microsoft.playwright.Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE));
    }

    public static void forSelectorHidden(Page page, String selector) {
        page.locator(selector).waitFor(new com.microsoft.playwright.Locator.WaitForOptions()
                .setState(WaitForSelectorState.HIDDEN));
    }
}
