# Java Playwright Industrial Framework for VS Code

Production-ready **Playwright + Java** test automation framework built with a clean industrial structure, reusable page objects, config-driven execution, reporting, screenshots, and GitHub Actions CI/CD.

## Tech Stack

- Java 17
- Maven
- Playwright
- JUnit 5
- SLF4J + Logback
- Extent Reports
- Allure support
- GitHub Actions

## Project Structure

```text
.
├── .github/workflows/
│   └── ci-cd.yml
├── src/
│   ├── main/java/com/playwright/framework/
│   │   ├── config/
│   │   │   ├── ConfigManager.java
│   │   │   └── FrameworkConfig.java
│   │   ├── driver/
│   │   │   └── PlaywrightManager.java
│   │   ├── pages/
│   │   │   ├── BasePage.java
│   │   │   └── HomePage.java
│   │   └── utils/
│   │       └── ScreenshotUtil.java
│   └── test/java/com/playwright/framework/
│       ├── reports/
│       │   ├── ExtentManager.java
│       │   ├── ExtentReportExtension.java
│       │   └── ExtentTestManager.java
│       └── tests/
│           ├── BaseTest.java
│           └── HomePageTest.java
└── src/test/resources/
    ├── config/framework.properties
    └── logback-test.xml
```

## Folder Purpose

| Folder | Purpose |
|---|---|
| `config` | Loads runtime config from properties, system props, or environment variables |
| `driver` | Manages Playwright, browser, and page lifecycle |
| `pages` | Page Object Model classes |
| `utils` | Reusable helpers like screenshots |
| `tests` | JUnit test classes |
| `reports` | Extent Report integration |
| `resources/config` | Default execution settings |
| `resources` | Test logging configuration |

## Main Classes

- `ConfigManager` — loads `baseUrl`, `browser`, `headless`, `timeoutMs`, `slowMo`
- `FrameworkConfig` — typed config record
- `PlaywrightManager` — initializes and closes browser/page instances
- `BasePage` — shared page actions
- `HomePage` — sample page object
- `BaseTest` — setup/teardown and failure screenshot handling
- `HomePageTest` — sample smoke/regression tests
- `ExtentReportExtension` — attaches test results to Extent Reports

## How It Works

1. `BaseTest` loads config and opens the browser.
2. `PlaywrightManager` creates a browser page.
3. Tests use page objects like `HomePage`.
4. On failure, screenshots are saved under `test-results/screenshots`.
5. Extent report output goes to `test-output/ExtentReport.html`.

## Configuration

Default config is in:

```text
src/test/resources/config/framework.properties
```

Example values:

```properties
base.url=https://playwright.dev
browser=chromium
headless=true
timeout.ms=30000
slow.mo=0
```

Override with JVM parameters:

```bash
mvn test -Dbrowser=firefox -Dheadless=false -DbaseUrl=https://example.com -DslowMo=200 -DtimeoutMs=45000
```

## Running Tests in VS Code

1. Open the project in VS Code.
2. Install these extensions:
   - Java Extension Pack
   - Maven for Java
   - Test Runner for Java
3. Make sure Java 17 and Maven are installed on your machine.
4. Open the project folder in VS Code using `File > Open Folder`.
5. Wait for VS Code to finish indexing and resolving Maven dependencies.
6. Run Maven tests from terminal:
   ```bash
   mvn test
   ```
7. Run specific suites:
   ```bash
   mvn test -Psmoke
   mvn test -Pregression
   ```
8. Run tests from the VS Code Testing panel if JUnit tests are detected.

## Adding a New Test

1. Create a page class under `src/main/java/com/playwright/framework/pages/`
2. Add actions and locators there
3. Create a test class under `src/test/java/com/playwright/framework/tests/`
4. Extend `BaseTest`
5. Tag with `@Tag("smoke")` or `@Tag("regression")`

Example:

```java
@Tag("smoke")
@Test
void shouldOpenHomePage() {
    assertTrue(homePage.title().contains("Playwright"));
}
```

## CI/CD

Workflow file:

```text
.github/workflows/ci-cd.yml
```

Pipeline:
- checks out code
- sets up JDK 17
- compiles project
- installs Playwright browsers
- runs smoke and regression suites
- uploads reports and screenshots
- builds release artifacts for tags like `v1.0.0`

## Useful Commands

```bash
mvn clean test-compile
mvn test
mvn test -Psmoke
mvn test -Pregression
mvn -DskipTests package
mvn versions:display-dependency-updates
mvn versions:display-plugin-updates
```
