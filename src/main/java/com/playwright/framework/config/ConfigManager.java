package com.playwright.framework.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Layered configuration:
 *   1. System property (highest)
 *   2. OS environment variable
 *   3. Env-specific properties  (config/framework-{env}.properties)
 *   4. Base properties          (config/framework.properties)
 *   5. Project-root .env file
 *   6. Hard-coded default       (lowest)
 *
 * Env is selected via {@code -Denv=qa} or {@code ENV=qa}.
 * Use {@link #secret(String)} for credentials — those never fall back to
 * property files, only to env vars / system properties.
 */
public final class ConfigManager {
    private static final String BASE_CONFIG = "config/framework.properties";
    private static final String ENV_CONFIG_FORMAT = "config/framework-%s.properties";
    private static final Path DOTENV_PATH = Path.of(".env");

    private static final ConcurrentMap<String, Properties> CACHE = new ConcurrentHashMap<>();

    private ConfigManager() {
    }

    public static FrameworkConfig load() {
        String env = read("env", "");
        Properties merged = CACHE.computeIfAbsent(env, ConfigManager::loadLayered);
        return new FrameworkConfig(
                readWith(merged, "base.url", "https://playwright.dev"),
                readWith(merged, "browser", "chromium"),
                Boolean.parseBoolean(readWith(merged, "headless", "true")),
                Integer.parseInt(readWith(merged, "timeout.ms", "30000")),
                Integer.parseInt(readWith(merged, "slow.mo", "0")),
                env,
                Boolean.parseBoolean(readWith(merged, "trace.on.failure", "true")),
                Boolean.parseBoolean(readWith(merged, "video.on.failure", "true"))
        );
    }

    /**
     * Fetches a secret from environment variables or system properties only.
     * Never reads from properties files. Throws if not defined.
     */
    public static String secret(String key) {
        String value = System.getProperty(key);
        if (value == null || value.isBlank()) {
            value = System.getenv(toEnvKey(key));
        }
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Secret '" + key + "' is not set. Provide it via env var "
                    + toEnvKey(key) + " or -D" + key + "=...");
        }
        return value.trim();
    }

    /* -------------------- internals -------------------- */

    private static Properties loadLayered(String env) {
        Properties merged = new Properties();
        loadFromDotEnv(merged);
        loadFromClasspath(merged, BASE_CONFIG);
        if (env != null && !env.isBlank()) {
            loadFromClasspath(merged, String.format(ENV_CONFIG_FORMAT, env));
        }
        return merged;
    }

    private static void loadFromClasspath(Properties target, String resource) {
        try (InputStream in = ConfigManager.class.getClassLoader().getResourceAsStream(resource)) {
            if (in != null) {
                target.load(in);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load configuration " + resource, exception);
        }
    }

    private static void loadFromDotEnv(Properties target) {
        if (!Files.isRegularFile(DOTENV_PATH)) {
            return;
        }
        try {
            for (String line : Files.readAllLines(DOTENV_PATH)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                int eq = trimmed.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                String key = trimmed.substring(0, eq).trim();
                String value = trimmed.substring(eq + 1).trim();
                if (value.length() >= 2
                        && ((value.startsWith("\"") && value.endsWith("\""))
                            || (value.startsWith("'") && value.endsWith("'")))) {
                    value = value.substring(1, value.length() - 1);
                }
                target.putIfAbsent(key, value);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load .env", exception);
        }
    }

    private static String read(String key, String defaultValue) {
        return readWith(new Properties(), key, defaultValue);
    }

    private static String readWith(Properties fileValues, String key, String defaultValue) {
        String sysValue = System.getProperty(toSystemKey(key));
        if (sysValue != null && !sysValue.isBlank()) {
            return sysValue.trim();
        }
        String envValue = System.getenv(toEnvKey(key));
        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
        }
        String fileValue = fileValues.getProperty(key);
        if (fileValue != null && !fileValue.isBlank()) {
            return fileValue.trim();
        }
        return defaultValue;
    }

    private static String toSystemKey(String fileKey) {
        return switch (fileKey) {
            case "base.url" -> "baseUrl";
            case "timeout.ms" -> "timeoutMs";
            case "slow.mo" -> "slowMo";
            case "trace.on.failure" -> "traceOnFailure";
            case "video.on.failure" -> "videoOnFailure";
            default -> fileKey;
        };
    }

    private static String toEnvKey(String fileKey) {
        return fileKey.replace('.', '_').toUpperCase();
    }
}

