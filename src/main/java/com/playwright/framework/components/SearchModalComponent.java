package com.playwright.framework.components;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.playwright.framework.ui.UiElement;

/**
 * The DocSearch modal (Algolia) opened from the header. Encapsulates the input
 * and result rows so tests never need to know the underlying selectors.
 */
public class SearchModalComponent extends BaseComponent {

    public SearchModalComponent(Page page) {
        super(page, page.locator(".DocSearch-Modal, [role='dialog']").first(), "SearchModal");
    }

    public UiElement input() {
        return $(page.getByRole(AriaRole.SEARCHBOX).first(), "search input");
    }

    public UiElement results() {
        return $("[class*='DocSearch-Hits'] a, [role='listbox'] a", "results");
    }

    public SearchModalComponent search(String query) {
        input().waitUntilVisible().type(query);
        return this;
    }

    public UiElement resultAt(int index) {
        return results().nth(index);
    }

    public void close() {
        page.keyboard().press("Escape");
        rootElement().waitUntilHidden();
    }
}
