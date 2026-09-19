package com.playwright.framework.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.playwright.framework.ui.UiElement;
import com.playwright.framework.ui.Waits;

/**
 * Represents the "Get started" documentation landing page on playwright.dev.
 */
public class DocsPage extends BasePage {

    public DocsPage(Page page) {
        super(page);
    }

    public UiElement pageHeading() {
        return $(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setLevel(1)).first(),
                "docs H1");
    }

    public UiElement sidebar() {
        return $("nav.theme-doc-sidebar-container, aside", "docs sidebar");
    }

    public UiElement sidebarItem(String name) {
        return $(page.getByRole(AriaRole.NAVIGATION)
                        .getByRole(AriaRole.LINK, new com.microsoft.playwright.Locator.GetByRoleOptions().setName(name))
                        .first(),
                "sidebar item '" + name + "'");
    }

    public DocsPage waitUntilLoaded() {
        Waits.forUrlContains(page, "/docs/");
        pageHeading().waitUntilVisible();
        return this;
    }
}
