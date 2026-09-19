# Playwright Java Framework (Industrial Structure)

Production-oriented Playwright + Java automation framework with layered design, layered config + secrets management, reusable components, fluent locators, artifact capture (trace / video / screenshot), storage-state auth reuse, parallel & sharded execution, and GitHub Actions CI/CD.

Supported browsers: `chromium`, `firefox`, `webkit`.

## 1. Project Structure

```text
.
├── .github/workflows/
│   └── ci-cd.yml                       # Sharded CI/CD pipeline (browser × suite matrix)
├── src/main/java/com/playwright/framework/
│   ├── auth/
│   │   └── AuthState.java              # Persist / reload storageState per user
│   ├── components/
│   │   ├── BaseComponent.java          # Root-scoped UI region
│   │   ├── HeaderComponent.java        # Top nav
│   │   ├── SearchModalComponent.java   # DocSearch dialog
│   │   └── TableComponent.java         # Reusable table wrapper
│   ├── config/
│   │   ├── ConfigManager.java          # Layered config (-D > env > env-file > base > .env)
│   │   └── FrameworkConfig.java        # Typed config record
│   ├── driver/
│   │   ├── PlaywrightManager.java      # Browser/context/page lifecycle + trace/video
│   │   └── SessionOptions.java         # Per-session runtime options
│   ├── pages/
│   │   ├── BasePage.java               # Fluent lookups + header()
│   │   ├── HomePage.java
│   │   ├── DocsPage.java
│   │   └── ApiPage.java
│   ├── ui/
│   │   ├── UiElement.java              # Fluent Locator wrapper (actions/waits/asserts)
│   │   └── Waits.java                  # Page-level wait helpers
│   └── utils/
│       └── ScreenshotUtil.java         # Failure screenshots
├── src/test/java/com/playwright/framework/
│   ├── reports/                        # Extent report listeners
│   └── tests/
│       ├── BaseTest.java               # Setup / teardown / artifact capture
│       ├── HomePageTest.java
│       ├── Flaky.java                  # @Flaky annotation
│       ├── QuarantineListener.java     # Soft-fail flaky tests via -Dquarantine.mode=true
│       └── NetworkRetryAnalyzer.java   # Retries transient failures only
├── src/test/resources/
│   ├── config/
│   │   ├── framework.properties        # Base config
│   │   └── framework-qa.properties     # Sample environment overlay
│   ├── testng.xml                      # Optional local TestNG suite
│   └── logback-test.xml
├── .env.example                        # Copy to .env (gitignored)
├── .dockerignore
├── .gitignore
├── Dockerfile                          # Reproducible runner (all browsers preinstalled)
├── pom.xml
└── README.md
```

## 2. Prerequisites

1. Java 17+
2. Maven 3.8+
3. GitHub repository (for CI/CD)

## 3. Quick Start

```bash
mvn clean test-compile
mvn exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install chromium"
mvn test -Psmoke
```

## 4. Usage Cheat Sheet

### Run tests

```bash
# Full test run (default browser from config)
mvn test

# Named suite via Maven profile
mvn test -Psmoke
mvn test -Pregression

# Pick browser(s) — the DataProvider filters to what you list
mvn test -Psmoke -Dbrowsers=chromium
mvn test -Psmoke -Dbrowsers=chromium,firefox,webkit

# Headed / slow-motion / custom timeout
mvn test -Psmoke -Dheadless=false -DslowMo=200 -DtimeoutMs=45000

# Override baseUrl on the fly
mvn test -Psmoke -DbaseUrl=https://staging.example.com

# Tune intra-JVM parallelism (Surefire threads)
mvn test -Psmoke -Dbrowsers=chromium -Dsurefire.threadCount=4

# Package without running tests
mvn -DskipTests package
```

### Environments (layered config)

Precedence — highest wins:

1. `-Dkey=value` system property
2. `KEY_WITH_UNDERSCORES` OS environment variable
3. `config/framework-{env}.properties` (activated with `-Denv=<name>`)
4. `config/framework.properties`
5. Project-root `.env` file
6. Hard-coded default

```bash
# Run against the qa overlay
mvn test -Denv=qa -Psmoke

# Toggle Playwright artifacts
mvn test -Psmoke -DtraceOnFailure=false -DvideoOnFailure=false
```

### Secrets

`ConfigManager.secret("admin.password")` reads **only** from env vars / `-D` — never from properties files. Missing values throw at load time.

```powershell
# PowerShell
$env:ADMIN_PASSWORD = "s3cr3t"
mvn test -Psmoke
```
```bash
# Bash
ADMIN_PASSWORD=s3cr3t mvn test -Psmoke
# or
mvn test -Psmoke -Dadmin.password=s3cr3t
```

### Failure artifacts

On a failing test, `BaseTest.tearDown()` writes:

| Artifact | Location |
|---|---|
| Screenshot | `test-results/screenshots/<test>-<browser>-<ts>.png` |
| Playwright trace | `test-results/traces/<test>-<browser>.zip` |
| Video (`.webm`)  | `test-results/videos/<hash>.webm` |

Open a trace locally:

```bash
mvn exec:java -Dexec.mainClass=com.microsoft.playwright.CLI \
              -Dexec.args="show-trace test-results/traces/<test>-<browser>.zip"
```

Passing tests leave no trace / video (auto-deleted).

### Reusing login (`storageState`)

```java
import com.playwright.framework.auth.AuthState;
import com.playwright.framework.driver.PlaywrightManager;
import com.playwright.framework.driver.SessionOptions;

// One-off: perform UI login and save state
if (!AuthState.exists("admin")) {
    PlaywrightManager.initialize("chromium");
    loginUi("admin", ConfigManager.secret("admin.password"));
    AuthState.save("admin");
    PlaywrightManager.shutdown();
}

// Every test after that starts already logged in
PlaywrightManager.initialize(SessionOptions.defaults()
        .withBrowser("chromium")
        .withStorageState(AuthState.forUser("admin")));
```

State files live under `.auth/` (gitignored).

### Fluent UI actions

```java
homePage.header()
        .openSearch()
        .search("locator")
        .resultAt(0)
        .shouldBeVisible()
        .click();

new DocsPage(page)
        .waitUntilLoaded()
        .sidebarItem("Writing tests")
        .click();
```

Every action / assertion is logged; wrap with your own reporter listeners if you need Allure steps.

### Retries & quarantine

The retry analyzer only retries **transient** failures (network errors, Playwright timeouts, dropped browser/context). `AssertionError`, `NullPointerException`, and other programming errors are **never** retried.

```bash
# Bump retries for all tests (default: 2)
mvn test -Psmoke -Dretry.max=3
```

Mark known-flaky tests with `@Flaky` so they're clearly labelled and get their own retry budget:

```java
@Test(retryAnalyzer = NetworkRetryAnalyzer.class, groups = "flaky")
@Flaky(value = "JIRA-1234", maxRetries = 3)
public void searchSometimesFlakes() { ... }
```

Quarantine mode converts failures in `@Flaky` tests into `SKIP` so a known-broken test doesn't block the pipeline while it's being fixed. Non-flaky tests are unaffected.

```bash
# Run only the quarantined suite and never fail the build on their failures
mvn test -Pquarantine -Dquarantine.mode=true

# Regular runs still fail on @Flaky failures (opt-in soft-fail)
mvn test -Psmoke
```

### Docker (reproducible local runs)

```bash
# Build the image (multi-stage, uses Microsoft's Playwright Java base image with all browsers preinstalled)
docker build -t playwright-java-framework .

# Default: smoke on chromium, headless, artifacts written to ./test-results
docker run --rm -v "$PWD/test-results:/app/test-results" playwright-java-framework

# Regression on firefox
docker run --rm -v "$PWD/test-results:/app/test-results" \
    -e BROWSERS=firefox playwright-java-framework \
    mvn -B -ntp test -Pregression -Dheadless=true -Dbrowsers=firefox

# Interactive shell
docker run --rm -it --entrypoint bash playwright-java-framework
```

### Maintenance

```bash
mvn versions:display-dependency-updates
mvn versions:display-plugin-updates
mvn versions:use-latest-releases
```

## 5. Adding a New Test

1. Create a page object in `src/main/java/com/playwright/framework/pages/` (extend `BasePage`, use `$(...)` for fluent lookups).
2. Extract reusable regions into components under `components/` (extend `BaseComponent`).
3. Add a test class in `src/test/java/com/playwright/framework/tests/` extending `BaseTest`.
4. Tag with TestNG `groups = "smoke"` / `"regression"`.
5. Validate locally:
   ```bash
   mvn test -Psmoke -Dbrowsers=chromium
   ```

## 6. Parallel & Sharded Execution

**Within a single JVM** — Surefire runs test methods in parallel:

- `<parallel>methods</parallel>`, `<threadCount>` and `<forkCount>1</forkCount>` are configured in `pom.xml`.
- One JVM = one browser (safe, avoids cross-browser races).
- Override with `-Dsurefire.threadCount=N`.

**Across CI runners** — GitHub Actions matrix in `.github/workflows/ci-cd.yml`:

```yaml
strategy:
  matrix:
    suite:   [ smoke, regression ]
    browser: [ chromium, firefox, webkit ]
```

Each cell runs `mvn test -P<suite> -Dbrowser=<b> -Dbrowsers=<b>` on its own runner and uploads shard-scoped artifacts. A `merge-reports` job aggregates all `allure-results/` into one artifact.

## 7. CI/CD

Workflow file: `.github/workflows/ci-cd.yml`

### CI (push / pull_request)
1. Checkout
2. Setup JDK 17
3. Compile
4. Install the shard's Playwright browser
5. Run the assigned suite for that browser
6. Upload shard artifacts (`surefire-reports`, `allure-results`, `test-results`)
7. Merge Allure results across shards

### CD (tag release)
1. Create a tag `v*`:
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```
2. Pipeline builds the JAR and creates a GitHub Release.

## 8. Troubleshooting

| Symptom | Fix |
|---|---|
| `Secret 'x' is not set` | Export the env var (`X` → uppercased, dots → underscores) or pass `-Dx=...`. |
| Trace / video missing on failure | Ensure `trace.on.failure=true` / `video.on.failure=true` in `framework.properties` (defaults). |
| Cross-browser flakiness in parallel | Keep one browser per JVM; shard browsers across CI runners instead. |
| Locators failing after a site redesign | Prefer role-based lookups (`page.getByRole(...)`) inside components; update the component, not every test. |

