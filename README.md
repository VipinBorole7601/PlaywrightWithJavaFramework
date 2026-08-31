# Playwright Java Framework (Industrial Structure)

Production-ready Playwright + Java automation framework with layered design, config management, reusable page objects, test tagging, artifact capture, and GitHub Actions CI/CD.
• chromium → Chrome/Edge engine  
• firefox → Firefox engine  
• webkit → Safari-like engine (useful for Apple-browser compatibility testing)

## 1. Project Structure

```text
.
├── .github/workflows/
│   └── ci-cd.yml                       # CI/CD pipeline
├── src/main/java/com/playwright/framework/
│   ├── config/
│   │   ├── ConfigManager.java          # Reads config from file/system/env
│   │   └── FrameworkConfig.java        # Typed config record
│   ├── driver/
│   │   └── PlaywrightManager.java      # Browser/page lifecycle
│   ├── pages/
│   │   ├── BasePage.java               # Common page methods
│   │   └── HomePage.java               # Sample page object
│   └── utils/
│       └── ScreenshotUtil.java         # Failure screenshots
├── src/test/java/com/playwright/framework/tests/
│   ├── BaseTest.java                   # Test hooks and setup/teardown
│   └── HomePageTest.java               # Smoke/regression sample tests
├── src/test/resources/
│   ├── config/framework.properties     # Default runtime config
│   └── logback-test.xml                # Logging config
├── .gitignore
├── pom.xml
└── README.md
```

## 2. Prerequisites

1. Java 17+
2. Maven 3.8+
3. GitHub repository (for CI/CD usage)

## 3. Manual Setup Steps (Same Project)

1. Clone/open this project.
2. Compile test sources:
   ```bash
   mvn clean test-compile
   ```
3. Install Playwright browser:
   ```bash
   mvn exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install chromium"
   ```
4. Run all tests:
   ```bash
   mvn test
   ```
5. Run smoke suite:
   ```bash
   mvn test -Psmoke
   ```
6. Run regression suite:
   ```bash
   mvn test -Pregression
   ```

## 4. Runtime Configuration

Default values are in:
`src/test/resources/config/framework.properties`

Override with JVM params:

```bash
mvn test -Dbrowser=firefox -Dheadless=false -DbaseUrl=https://example.com -DslowMo=200 -DtimeoutMs=45000
```

Supported browsers: `chromium`, `firefox`, `webkit`

## 5. Manual Steps to Add New Test

1. Create a page object in `src/main/java/com/playwright/framework/pages/`.
2. Add locators and business actions in that class.
3. Create test class in `src/test/java/com/playwright/framework/tests/`.
4. Extend `BaseTest` and use `@Tag("smoke")` or `@Tag("regression")`.
5. Run with Maven profile to validate:
   ```bash
   mvn test -Psmoke
   ```

## 6. CI/CD Steps

Workflow file: `.github/workflows/ci-cd.yml`

### CI (push / pull_request)
1. Checkout code
2. Setup JDK 17
3. Compile project
4. Install Playwright browser
5. Run smoke tests
6. Run regression tests
7. Upload artifacts (`surefire`, `allure`, screenshots)

### CD (tag release)
1. Create tag with format `v*` (example: `v1.0.0`)
2. Push tag:
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```
3. Pipeline builds JAR and creates GitHub Release with artifact upload.

## 7. Useful Commands

```bash
mvn -DskipTests package
mvn test -Dheadless=true
mvn test -Dbrowser=webkit
mvn test "-Pchromium,firefox,webkit" -Dheadless=false  #to run all at once
```

# This is how you can check and update the dependency-updates , plugin updates and releases updates

mvn versions:display-dependency-updates
mvn versions:display-plugin-updates
mvn versions:use-latest-releases
