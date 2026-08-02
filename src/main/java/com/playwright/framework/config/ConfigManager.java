package com.playwright.framework.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ConfigManager {
    private static final String CONFIG_PATH = "config/framework.properties";
    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream inputStream = ConfigManager.class.getClassLoader().getResourceAsStream(CONFIG_PATH)) {
            if (inputStream == null) {
                throw new IllegalStateException("Configuration file not found: " + CONFIG_PATH);
            }
            PROPERTIES.load(inputStream);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load framework configuration", exception);
        }
    }

    private ConfigManager() {
    }

    public static FrameworkConfig load() {
        return new FrameworkConfig(
                read("base.url", "https://playwright.dev"),
                read("browser", "chromium"),
                Boolean.parseBoolean(read("headless", "true")),
                Integer.parseInt(read("timeout.ms", "30000")),
                Integer.parseInt(read("slow.mo", "0"))
        );
    }

    private static String read(String key, String defaultValue) {
        String systemValue = System.getProperty(mapSystemKey(key));
        if (systemValue != null && !systemValue.isBlank()) {
            return systemValue.trim();
        }

        String envValue = System.getenv(mapEnvKey(key));
        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
        }

        String fileValue = PROPERTIES.getProperty(key);
        if (fileValue != null && !fileValue.isBlank()) {
            return fileValue.trim();
        }
        return defaultValue;
    }

    private static String mapSystemKey(String fileKey) {
        return switch (fileKey) {
            case "base.url" -> "baseUrl";
            case "timeout.ms" -> "timeoutMs";
            case "slow.mo" -> "slowMo";
            default -> fileKey;
        };
    }

    private static String mapEnvKey(String fileKey) {
        return switch (fileKey) {
            case "base.url" -> "BASE_URL";
            case "timeout.ms" -> "TIMEOUT_MS";
            case "slow.mo" -> "SLOW_MO";
            default -> fileKey.toUpperCase();
        };
    }
}
