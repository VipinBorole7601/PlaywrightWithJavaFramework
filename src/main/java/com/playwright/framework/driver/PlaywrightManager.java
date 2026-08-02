package com.playwright.framework.driver;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.playwright.framework.config.ConfigManager;
import com.playwright.framework.config.FrameworkConfig;

public final class PlaywrightManager {
    private static final ThreadLocal<Playwright> PLAYWRIGHT = new ThreadLocal<>();
    private static final ThreadLocal<Browser> BROWSER = new ThreadLocal<>();
    private static final ThreadLocal<Page> PAGE = new ThreadLocal<>();

    private PlaywrightManager() {
    }

    public static void initialize() {
        FrameworkConfig config = ConfigManager.load();
        Playwright playwright = Playwright.create();
        Browser browser = launchBrowser(playwright, config);
        Page page = browser.newContext(new Browser.NewContextOptions()
                        .setViewportSize(1920, 1080))
                .newPage();
        page.setDefaultTimeout(config.timeoutMs());

        PLAYWRIGHT.set(playwright);
        BROWSER.set(browser);
        PAGE.set(page);
    }

    public static Page page() {
        Page page = PAGE.get();
        if (page == null) {
            throw new IllegalStateException("Page is not initialized. Call initialize() first.");
        }
        return page;
    }

    public static void shutdown() {
        if (PAGE.get() != null) {
            PAGE.get().context().close();
            PAGE.remove();
        }
        if (BROWSER.get() != null) {
            BROWSER.get().close();
            BROWSER.remove();
        }
        if (PLAYWRIGHT.get() != null) {
            PLAYWRIGHT.get().close();
            PLAYWRIGHT.remove();
        }
    }

    private static Browser launchBrowser(Playwright playwright, FrameworkConfig config) {
        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
                .setHeadless(config.headless())
                .setSlowMo((double) config.slowMo());

        return switch (config.browser().toLowerCase()) {
            case "chromium" -> playwright.chromium().launch(options);
            case "firefox" -> playwright.firefox().launch(options);
            case "webkit" -> playwright.webkit().launch(options);
            default -> throw new IllegalArgumentException("Unsupported browser: " + config.browser());
        };
    }
}
