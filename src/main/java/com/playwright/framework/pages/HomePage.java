package com.playwright.framework.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.playwright.framework.ui.UiElement;

public class HomePage extends BasePage {

    public HomePage(Page page) {
        super(page);
    }

    public UiElement getStartedLink() {
        return $(page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Get started")).first(),
                "Get started link");
    }

    public UiElement heroHeading() {
        return $(page.getByRole(AriaRole.HEADING).first(), "hero heading");
    }

    public boolean isDocsLinkVisible() {
        return getStartedLink().isVisible();
    }

    public DocsPage openGetStarted() {
        getStartedLink().click();
        return new DocsPage(page);
    }
}

