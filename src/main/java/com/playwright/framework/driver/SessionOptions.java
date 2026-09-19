package com.playwright.framework.driver;

import java.nio.file.Path;

/**
 * Runtime options for a Playwright session. Lets tests / infrastructure customise
 * the browser, storage state, video and tracing without touching system properties.
 */
public final class SessionOptions {
    private String browserOverride;
    private Path storageStatePath;
    private boolean recordVideo;
    private boolean recordTrace;

    private SessionOptions() {
    }

    public static SessionOptions defaults() {
        return new SessionOptions();
    }

    public SessionOptions withBrowser(String browser) {
        this.browserOverride = browser;
        return this;
    }

    public SessionOptions withStorageState(Path path) {
        this.storageStatePath = path;
        return this;
    }

    public SessionOptions withVideoRecording(boolean enabled) {
        this.recordVideo = enabled;
        return this;
    }

    public SessionOptions withTracing(boolean enabled) {
        this.recordTrace = enabled;
        return this;
    }

    public String browserOverride() {
        return browserOverride;
    }

    public Path storageStatePath() {
        return storageStatePath;
    }

    public boolean recordVideo() {
        return recordVideo;
    }

    public boolean recordTrace() {
        return recordTrace;
    }
}
