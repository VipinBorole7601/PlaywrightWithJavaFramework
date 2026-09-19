package com.playwright.framework.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.playwright.framework.components.HeaderComponent;
import com.playwright.framework.ui.UiElement;
import com.playwright.framework.ui.Waits;

public abstract class BasePage {
    protected final Page page;
    private HeaderComponent header;

    protected BasePage(Page page) {
        this.page = page;
    }

    public HeaderComponent header() {
        if (header == null) {
            header = new HeaderComponent(page);
        }
        return header;
    }

    public String title() {
        return page.title();
    }

    public String url() {
        return page.url();
    }

    protected void open(String path) {
        page.navigate(path);
        Waits.forLoad(page);
    }

    protected Locator locator(String selector) {
        return page.locator(selector);
    }

    /** Fluent element lookup by CSS selector. */
    protected UiElement $(String selector, String description) {
        return UiElement.of(page.locator(selector), description);
    }

    /** Fluent wrapper around an already-built Locator. */
    protected UiElement $(Locator locator, String description) {
        return UiElement.of(locator, description);
    }
}

