package com.playwright.framework.components;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.playwright.framework.ui.UiElement;

/**
 * The top navigation bar rendered on every playwright.dev page.
 * Exposes primary nav links, GitHub link, and the search trigger.
 */
public class HeaderComponent extends BaseComponent {

    public HeaderComponent(Page page) {
        super(page, page.getByRole(AriaRole.BANNER).first(), "Header");
    }

    public UiElement docsLink() {
        return $(page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Docs")).first(), "Docs link");
    }

    public UiElement apiLink() {
        return $(page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("API")).first(), "API link");
    }

    public UiElement communityLink() {
        return $(page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Community")).first(), "Community link");
    }

    public UiElement gitHubLink() {
        return $(page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("GitHub repository")).first(), "GitHub link");
    }

    public UiElement searchTrigger() {
        return $(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Search")).first(), "Search button");
    }

    public SearchModalComponent openSearch() {
        searchTrigger().click();
        SearchModalComponent modal = new SearchModalComponent(page);
        modal.rootElement().waitUntilVisible();
        return modal;
    }
}
