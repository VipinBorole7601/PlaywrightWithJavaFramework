package com.playwright.framework.pages;

import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.Page;

public class HomePage extends BasePage {
    public HomePage(Page page) {
        super(page);
    }

    public String title() {
        return page.title();
    }

    public boolean isDocsLinkVisible() {
        return page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Get started")).isVisible();
    }



}
