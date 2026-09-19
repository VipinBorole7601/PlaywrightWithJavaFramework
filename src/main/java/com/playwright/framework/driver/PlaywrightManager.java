package com.playwright.framework.driver;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;
import com.playwright.framework.config.ConfigManager;
import com.playwright.framework.config.FrameworkConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class PlaywrightManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlaywrightManager.class);

    private static final Path VIDEO_DIR = Paths.get("test-results", "videos");
    private static final Path TRACE_DIR = Paths.get("test-results", "traces");

    private static final ThreadLocal<Playwright> PLAYWRIGHT = new ThreadLocal<>();
    private static final ThreadLocal<Browser> BROWSER = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> CONTEXT = new ThreadLocal<>();
    private static final ThreadLocal<Page> PAGE = new ThreadLocal<>();
    private static final ThreadLocal<String> ACTIVE_BROWSER = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> TRACING_ACTIVE = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<Boolean> VIDEO_ENABLED = ThreadLocal.withInitial(() -> false);

    private PlaywrightManager() {
    }

    public static void initialize() {
        initialize(SessionOptions.defaults());
    }

    public static void initialize(String browserOverride) {
        initialize(SessionOptions.defaults().withBrowser(browserOverride));
    }

    public static void initialize(SessionOptions options) {
        FrameworkConfig config = ConfigManager.load();
        String browserName = (options.browserOverride() == null || options.browserOverride().isBlank())
                ? config.browser()
                : options.browserOverride().trim();

        boolean videoEnabled = options.recordVideo() || config.videoOnFailure();
        boolean traceEnabled = options.recordTrace() || config.traceOnFailure();

        Playwright playwright = Playwright.create();
        Browser browser = launchBrowser(playwright, browserName, config);

        Browser.NewContextOptions ctxOptions = new Browser.NewContextOptions()
                .setViewportSize(1920, 1080);

        if (videoEnabled) {
            ensureDir(VIDEO_DIR);
            ctxOptions.setRecordVideoDir(VIDEO_DIR);
        }
        if (options.storageStatePath() != null && Files.isRegularFile(options.storageStatePath())) {
            ctxOptions.setStorageStatePath(options.storageStatePath());
            LOGGER.info("Loaded storage state from {}", options.storageStatePath());
        }

        BrowserContext context = browser.newContext(ctxOptions);
        context.setDefaultTimeout(config.timeoutMs());

        if (traceEnabled) {
            ensureDir(TRACE_DIR);
            context.tracing().start(new Tracing.StartOptions()
                    .setScreenshots(true)
                    .setSnapshots(true)
                    .setSources(true));
            TRACING_ACTIVE.set(true);
        }

        Page page = context.newPage();
        page.setDefaultTimeout(config.timeoutMs());

        PLAYWRIGHT.set(playwright);
        BROWSER.set(browser);
        CONTEXT.set(context);
        PAGE.set(page);
        ACTIVE_BROWSER.set(browserName);
        VIDEO_ENABLED.set(videoEnabled);
    }

    public static Page page() {
        Page page = PAGE.get();
        if (page == null) {
            throw new IllegalStateException("Page is not initialized. Call initialize() first.");
        }
        return page;
    }

    public static BrowserContext context() {
        return CONTEXT.get();
    }

    public static String activeBrowser() {
        return ACTIVE_BROWSER.get();
    }

    /** Save a trace to {@code test-results/traces/<label>.zip}. Safe no-op if tracing wasn't started. */
    public static Path saveTrace(String label) {
        if (!Boolean.TRUE.equals(TRACING_ACTIVE.get()) || CONTEXT.get() == null) {
            return null;
        }
        ensureDir(TRACE_DIR);
        Path target = TRACE_DIR.resolve(sanitize(label) + ".zip");
        CONTEXT.get().tracing().stop(new Tracing.StopOptions().setPath(target));
        TRACING_ACTIVE.set(false);
        LOGGER.info("Trace saved to {}", target);
        return target;
    }

    public static void discardTrace() {
        if (!Boolean.TRUE.equals(TRACING_ACTIVE.get()) || CONTEXT.get() == null) {
            return;
        }
        CONTEXT.get().tracing().stop(new Tracing.StopOptions());
        TRACING_ACTIVE.set(false);
    }

    /**
     * Closes the current page/context/browser. When {@code keepVideo} is false,
     * the recorded video (if any) is deleted after context close.
     */
    public static Path shutdown(boolean keepVideo) {
        Page currentPage = PAGE.get();
        Path videoPath = null;

        try {
            if (currentPage != null && Boolean.TRUE.equals(VIDEO_ENABLED.get()) && currentPage.video() != null) {
                if (CONTEXT.get() != null) {
                    CONTEXT.get().close();
                }
                if (keepVideo) {
                    try {
                        videoPath = currentPage.video().path();
                        LOGGER.info("Video kept at {}", videoPath);
                    } catch (RuntimeException e) {
                        LOGGER.warn("Unable to resolve video path", e);
                    }
                } else {
                    try {
                        currentPage.video().delete();
                    } catch (RuntimeException e) {
                        LOGGER.debug("Video already gone: {}", e.getMessage());
                    }
                }
            } else if (CONTEXT.get() != null) {
                CONTEXT.get().close();
            }
        } finally {
            PAGE.remove();
            CONTEXT.remove();
            if (BROWSER.get() != null) {
                BROWSER.get().close();
                BROWSER.remove();
            }
            if (PLAYWRIGHT.get() != null) {
                PLAYWRIGHT.get().close();
                PLAYWRIGHT.remove();
            }
            ACTIVE_BROWSER.remove();
            TRACING_ACTIVE.remove();
            VIDEO_ENABLED.remove();
        }
        return videoPath;
    }

    /** Convenience: shutdown without keeping video (used by successful tests). */
    public static void shutdown() {
        shutdown(false);
    }

    private static Browser launchBrowser(Playwright playwright, String browserName, FrameworkConfig config) {
        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
                .setHeadless(config.headless())
                .setSlowMo((double) config.slowMo());

        return switch (browserName.toLowerCase()) {
            case "chromium" -> playwright.chromium().launch(options);
            case "firefox" -> playwright.firefox().launch(options);
            case "webkit" -> playwright.webkit().launch(options);
            default -> throw new IllegalArgumentException("Unsupported browser: " + browserName);
        };
    }

    private static void ensureDir(Path dir) {
        try {
            Files.createDirectories(dir);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to create directory " + dir, e);
        }
    }

    private static String sanitize(String value) {
        return value == null ? "trace" : value.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}


