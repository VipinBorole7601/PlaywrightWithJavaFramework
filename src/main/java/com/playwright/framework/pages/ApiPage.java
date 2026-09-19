package com.playwright.framework.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.playwright.framework.ui.UiElement;
import com.playwright.framework.ui.Waits;

/**
 * Represents the API reference section on playwright.dev.
 */
public class ApiPage extends BasePage {

    public ApiPage(Page page) {
        super(page);
    }

    public UiElement pageHeading() {
        return $(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setLevel(1)).first(),
                "API H1");
    }

    public UiElement classLink(String className) {
        return $(page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(className)).first(),
                "API class link '" + className + "'");
    }

    public ApiPage waitUntilLoaded() {
        Waits.forUrlContains(page, "/docs/api/");
        pageHeading().waitUntilVisible();
        return this;
    }
}
