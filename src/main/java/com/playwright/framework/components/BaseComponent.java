package com.playwright.framework.components;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.playwright.framework.ui.UiElement;

/**
 * Base class for reusable UI components (headers, modals, tables, etc.).
 * A component owns a {@code root} Locator and exposes child lookups scoped to it.
 * Components should never navigate — they only expose actions/queries on their region.
 */
public abstract class BaseComponent {
    protected final Page page;
    protected final Locator root;
    private final String name;

    protected BaseComponent(Page page, Locator root, String name) {
        this.page = page;
        this.root = root;
        this.name = name;
    }

    public String name() {
        return name;
    }

    public UiElement rootElement() {
        return UiElement.of(root, name);
    }

    protected UiElement $(String selector, String description) {
        return UiElement.of(root.locator(selector), name + " > " + description);
    }

    protected UiElement $(Locator locator, String description) {
        return UiElement.of(locator, name + " > " + description);
    }

    public boolean isDisplayed() {
        return root.isVisible();
    }
}
