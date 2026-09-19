package com.playwright.framework.auth;

import com.microsoft.playwright.BrowserContext;
import com.playwright.framework.driver.PlaywrightManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Persists and reloads Playwright storage state (cookies + localStorage) so
 * tests can skip the login UI. Each user gets a distinct file under {@code .auth/}.
 *
 * Typical flow:
 * <pre>{@code
 *   Path state = AuthState.forUser("admin");
 *   if (!AuthState.exists("admin")) {
 *       PlaywrightManager.initialize(browser);
 *       loginUi("admin", ConfigManager.secret("admin.password"));
 *       AuthState.save("admin");
 *       PlaywrightManager.shutdown();
 *   }
 *   PlaywrightManager.initialize(SessionOptions.defaults().withStorageState(state));
 * }</pre>
 */
public final class AuthState {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthState.class);
    private static final Path AUTH_DIR = Paths.get(".auth");

    private AuthState() {
    }

    public static Path forUser(String user) {
        return AUTH_DIR.resolve(sanitize(user) + ".json");
    }

    public static boolean exists(String user) {
        return Files.isRegularFile(forUser(user));
    }

    public static Path save(String user) {
        BrowserContext context = PlaywrightManager.context();
        if (context == null) {
            throw new IllegalStateException("No active BrowserContext. Call PlaywrightManager.initialize() first.");
        }
        Path target = forUser(user);
        try {
            Files.createDirectories(target.getParent());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to create auth dir " + AUTH_DIR, e);
        }
        context.storageState(new BrowserContext.StorageStateOptions().setPath(target));
        LOGGER.info("Storage state saved for user '{}' at {}", user, target);
        return target;
    }

    public static void clear(String user) {
        try {
            Files.deleteIfExists(forUser(user));
        } catch (Exception e) {
            LOGGER.warn("Unable to clear storage state for {}: {}", user, e.getMessage());
        }
    }

    private static String sanitize(String user) {
        return user == null ? "default" : user.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
