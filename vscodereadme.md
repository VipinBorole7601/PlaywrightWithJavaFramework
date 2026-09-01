# Java Playwright Frameworks for VS Code

Complete guide for **Playwright + Java** test automation with **three framework options**: TestNG, JUnit 5, and Cucumber. Each has its own setup, structure, and execution model.

---

## Table of Contents

1. [TestNG Setup](#testng-setup)
2. [JUnit 5 Setup](#junit-5-setup)
3. [Cucumber BDD Setup](#cucumber-bdd-setup)
4. [Configuration](#configuration)
5. [Running Tests in VS Code](#running-tests-in-vs-code)
6. [CI/CD](#cicd)

---

# TestNG Setup

**Best for:** Data-driven testing, parallel execution, complex test dependencies, multiple test suites.

## Tech Stack

- Java 17
- Maven
- Playwright
- **TestNG 7.10**
- SLF4J + Logback
- Extent Reports
- GitHub Actions

## Project Structure

```
src/
├── main/java/com/playwright/framework/
│   ├── config/
│   │   ├── ConfigManager.java
│   │   └── FrameworkConfig.java
│   ├── driver/
│   │   └── PlaywrightManager.java
│   ├── pages/
│   │   ├── BasePage.java
│   │   └── HomePage.java
│   └── utils/
│       └── ScreenshotUtil.java
└── test/java/com/playwright/framework/
    ├── reports/
    │   ├── ExtentManager.java
    │   ├── ExtentReportExtension.java
    │   └── ExtentTestManager.java
    └── tests/
        ├── BaseTest.java
        ├── HomePageTest.java
        └── NetworkRetryAnalyzer.java
```

## Dependencies (pom.xml)

```xml
<dependency>
    <groupId>com.microsoft.playwright</groupId>
    <artifactId>playwright</artifactId>
    <version>1.54.0</version>
</dependency>
<dependency>
    <groupId>org.testng</groupId>
    <artifactId>testng</artifactId>
    <version>7.10.2</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>com.aventstack</groupId>
    <artifactId>extentreports</artifactId>
    <version>5.1.2</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-api</artifactId>
    <version>2.0.17</version>
</dependency>
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <version>1.5.18</version>
    <scope>test</scope>
</dependency>
```

## Example Test (TestNG)

```java
@Listeners(ExtentReportExtension.class)
public class HomePageTest extends BaseTest {

    @DataProvider(name = "browsers")
    public Object[][] browsers() {
        return new Object[][] {
                {"chromium"},
                {"firefox"},
                {"webkit"}
        };
    }

    @Test(dataProvider = "browsers", groups = "smoke", retryAnalyzer = NetworkRetryAnalyzer.class)
    public void shouldOpenPlaywrightHomePage(String browser) {
        setUpForBrowser(browser);
        assertTrue(homePage.title().contains("Playwright"));
        assertTrue(homePage.isDocsLinkVisible());
    }

    @Test(dataProvider = "browsers", groups = "regression")
    public void shouldLoadTheHomePageWithoutErrors(String browser) {
        setUpForBrowser(browser);
        assertTrue(homePage.title().length() > 0);
    }
}
```

## Key Features

- **DataProviders** — run same test with different data/browsers
- **Retry Analyzer** — auto-retry on network failures
- **Test Groups** — organize tests by `smoke`, `regression`, etc.
- **Listeners** — attach report listeners to lifecycle events
- **Extent Reports** — detailed HTML reports with screenshots

## Run TestNG Tests

```bash
# Run all tests
mvn test

# Run smoke suite
mvn test -Dgroups=smoke

# Run specific test class
mvn test -Dtest=HomePageTest

# Run with browser override
mvn test -Dbrowser=firefox -Dheadless=false

# Run parallel (8 threads)
mvn test -Dparallel=methods -DthreadCount=8
```

---

# JUnit 5 Setup

**Best for:** Modern Java projects, streamlined syntax, strong IDE support, simple linear tests.

## Tech Stack

- Java 17
- Maven
- Playwright
- **JUnit 5 (Jupiter)**
- SLF4J + Logback
- Allure Reports
- GitHub Actions

## Project Structure

```
src/
├── main/java/com/playwright/framework/
│   ├── config/
│   │   ├── ConfigManager.java
│   │   └── FrameworkConfig.java
│   ├── driver/
│   │   └── PlaywrightManager.java
│   ├── pages/
│   │   ├── BasePage.java
│   │   └── HomePage.java
│   └── utils/
│       └── ScreenshotUtil.java
└── test/java/com/playwright/framework/
    ├── extensions/
    │   ├── PlaywrightExtension.java
    │   └── NetworkRetryExtension.java
    └── tests/
        ├── BaseTest.java
        ├── HomePageTest.java
        └── SearchTest.java
```

## Dependencies (pom.xml)

```xml
<dependency>
    <groupId>com.microsoft.playwright</groupId>
    <artifactId>playwright</artifactId>
    <version>1.54.0</version>
</dependency>
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.11.4</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-params</artifactId>
    <version>5.11.4</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>io.qameta.allure</groupId>
    <artifactId>allure-junit5</artifactId>
    <version>2.29.1</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-api</artifactId>
    <version>2.0.17</version>
</dependency>
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <version>1.5.18</version>
    <scope>test</scope>
</dependency>
```

## Example Test (JUnit 5)

```java
@ExtendWith(PlaywrightExtension.class)
@Tag("smoke")
@DisplayName("Home Page Tests")
public class HomePageTest extends BaseTest {

    @ParameterizedTest(name = "Browser: {0}")
    @ValueSource(strings = {"chromium", "firefox", "webkit"})
    @DisplayName("Should open Playwright home page")
    void shouldOpenPlaywrightHomePage(String browser) {
        setUpForBrowser(browser);
        assertTrue(homePage.title().contains("Playwright"));
        assertTrue(homePage.isDocsLinkVisible());
    }

    @Test
    @DisplayName("Should load page without errors")
    void shouldLoadPageWithoutErrors() {
        setUpForBrowser("chromium");
        assertTrue(homePage.title().length() > 0);
    }

    @ParameterizedTest
    @CsvSource({
            "chromium,true",
            "firefox,true",
            "webkit,false"
    })
    @DisplayName("Should verify browser compatibility")
    void shouldVerifyBrowserCompatibility(String browser, boolean isSupported) {
        setUpForBrowser(browser);
        assertEquals(isSupported, homePage.isSupported());
    }
}
```

## PlaywrightExtension

```java
public class PlaywrightExtension implements BeforeEachCallback, AfterEachCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlaywrightExtension.class);

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        FrameworkConfig config = ConfigManager.load();
        PlaywrightManager.initialize();
        LOGGER.info("Browser initialized: {}", config.browser());
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        if (context.getExecutionException().isPresent()) {
            ScreenshotUtil.capture(PlaywrightManager.page(), context.getDisplayName());
            LOGGER.error("Test failed: {}", context.getDisplayName());
        }
        PlaywrightManager.shutdown();
    }
}
```

## Key Features

- **@ParameterizedTest** — data-driven tests with cleaner syntax
- **@ExtendWith** — lifecycle management with extensions
- **@DisplayName** — human-readable test names
- **Allure Reports** — beautiful test result visualization
- **Modern annotations** — no listeners or test classes needed for simple tests

## Run JUnit 5 Tests

```bash
# Run all tests
mvn test

# Run by tag
mvn test -Dgroups=smoke

# Run specific test class
mvn test -Dtest=HomePageTest

# Run with parallel execution
mvn test -Dparallel=methods -DthreadCount=8

# Generate Allure report
mvn test
allure serve target/allure-results
```

---

# Cucumber BDD Setup

**Best for:** BDD workflows, non-technical stakeholders, living documentation, business-readable scenarios.

## Tech Stack

- Java 17
- Maven
- Playwright
- **Cucumber JVM**
- JUnit 5 (as runner)
- SLF4J + Logback
- Allure Reports
- GitHub Actions

## Project Structure

```
src/
├── main/java/com/playwright/framework/
│   ├── config/
│   │   ├── ConfigManager.java
│   │   └── FrameworkConfig.java
│   ├── driver/
│   │   └── PlaywrightManager.java
│   ├── pages/
│   │   ├── BasePage.java
│   │   └── HomePage.java
│   └── utils/
│       └── ScreenshotUtil.java
└── test/
    ├── java/com/playwright/framework/
    │   ├── runners/
    │   │   └── CucumberRunner.java
    │   └── steps/
    │       ├── BaseStep.java
    │       ├── HomePageSteps.java
    │       └── Hooks.java
    └── resources/features/
        ├── home_page.feature
        └── search.feature
```

## Dependencies (pom.xml)

```xml
<dependency>
    <groupId>com.microsoft.playwright</groupId>
    <artifactId>playwright</artifactId>
    <version>1.54.0</version>
</dependency>
<dependency>
    <groupId>io.cucumber</groupId>
    <artifactId>cucumber-java</artifactId>
    <version>7.20.1</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>io.cucumber</groupId>
    <artifactId>cucumber-junit-platform-engine</artifactId>
    <version>7.20.1</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.junit.platform</groupId>
    <artifactId>junit-platform-suite</artifactId>
    <version>1.10.2</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>io.qameta.allure</groupId>
    <artifactId>allure-cucumber7-jvm</artifactId>
    <version>2.29.1</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-api</artifactId>
    <version>2.0.17</version>
</dependency>
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <version>1.5.18</version>
    <scope>test</scope>
</dependency>
```

## Example Feature File

```gherkin
# src/test/resources/features/home_page.feature

Feature: Playwright Home Page
  As a user
  I want to visit the Playwright home page
  So that I can learn about Playwright

  Background:
    Given I am on the Playwright home page

  @smoke
  Scenario Outline: Open home page on different browsers
    Given I am using <browser>
    When I navigate to the home page
    Then the page title should contain "Playwright"
    And the docs link should be visible

    Examples:
      | browser  |
      | chromium |
      | firefox  |
      | webkit   |

  @regression
  Scenario: Page loads without errors
    When I load the home page
    Then the page title should not be empty
    And I should see the getting started section
```

## Example Step Definitions

```java
// src/test/java/com/playwright/framework/steps/HomePageSteps.java

@DisplayName("Home Page Steps")
public class HomePageSteps extends BaseStep {

    private HomePage homePage;

    @Given("I am on the Playwright home page")
    public void setupHomePage() {
        FrameworkConfig config = ConfigManager.load();
        PlaywrightManager.initialize();
        PlaywrightManager.page().navigate(config.baseUrl());
        homePage = new HomePage(PlaywrightManager.page());
    }

    @Given("I am using {string}")
    public void setupBrowser(String browser) {
        System.setProperty("browser", browser);
        PlaywrightManager.shutdown();
        PlaywrightManager.initialize();
    }

    @When("I navigate to the home page")
    public void navigateToHomePage() {
        FrameworkConfig config = ConfigManager.load();
        PlaywrightManager.page().navigate(config.baseUrl());
    }

    @When("I load the home page")
    public void loadHomePage() {
        PlaywrightManager.page().reload();
    }

    @Then("the page title should contain {string}")
    public void verifyPageTitle(String expectedText) {
        assertTrue(homePage.title().contains(expectedText),
                "Expected title to contain: " + expectedText);
    }

    @Then("the page title should not be empty")
    public void verifyPageTitleNotEmpty() {
        assertTrue(homePage.title().length() > 0,
                "Page title should not be empty");
    }

    @Then("the docs link should be visible")
    public void verifyDocsLinkVisible() {
        assertTrue(homePage.isDocsLinkVisible(),
                "Expected docs link to be visible");
    }

    @Then("I should see the getting started section")
    public void verifyGettingStartedSection() {
        assertTrue(homePage.isGettingStartedSectionVisible(),
                "Expected getting started section to be visible");
    }
}
```

## Hooks (Setup/Teardown)

```java
// src/test/java/com/playwright/framework/steps/Hooks.java

public class Hooks extends BaseStep {

    private static final Logger LOGGER = LoggerFactory.getLogger(Hooks.class);

    @Before
    public void before(Scenario scenario) {
        LOGGER.info("Starting scenario: {}", scenario.getName());
    }

    @After
    public void after(Scenario scenario) {
        if (scenario.isFailed()) {
            ScreenshotUtil.capture(PlaywrightManager.page(), scenario.getName());
            LOGGER.error("Scenario failed: {}", scenario.getName());
        }
        PlaywrightManager.shutdown();
    }
}
```

## Cucumber Runner

```java
// src/test/java/com/playwright/framework/runners/CucumberRunner.java

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = PLUGIN_PUBLISH_QUIET_OPTION, value = "true")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.playwright.framework.steps")
public class CucumberRunner {
}
```

## Key Features

- **Gherkin syntax** — business-readable scenarios
- **Background** — common setup for all scenarios
- **Scenario Outline** — data-driven with Examples
- **Hooks** — Before/After lifecycle management
- **Step reusability** — write steps once, use everywhere
- **Allure integration** — detailed BDD reports

## Run Cucumber Tests

```bash
# Run all features
mvn test

# Run specific feature
mvn test -Dtest=CucumberRunner

# Run by tag
mvn test -Dcucumber.filter.tags="@smoke"

# Generate Allure report
mvn test
allure serve target/allure-results

# Run with parallel threads
mvn test -DthreadCount=4 -Dparallel=methods
```

---

# Configuration

All three frameworks share the same configuration system.

## Default Config

**File:** `src/test/resources/config/framework.properties`

```properties
base.url=https://playwright.dev
browser=chromium
headless=true
timeout.ms=30000
slow.mo=0
```

## Override via JVM Parameters

```bash
# Multiple parameters
mvn test \
  -Dbrowser=firefox \
  -Dheadless=false \
  -DbaseUrl=https://example.com \
  -DslowMo=200 \
  -DtimeoutMs=45000

# With profiles
mvn test -Psmoke -Dbrowser=firefox
```

## Override via Environment Variables

```bash
# Export before running
export BROWSER=firefox
export HEADLESS=false
export BASE_URL=https://example.com

mvn test
```

## ConfigManager

```java
// Loads in order: environment variables → JVM properties → framework.properties
FrameworkConfig config = ConfigManager.load();
String baseUrl = config.baseUrl();      // https://playwright.dev
String browser = config.browser();       // chromium
boolean headless = config.headless();    // true
int timeout = config.timeoutMs();        // 30000
```

---

# Running Tests in VS Code

## Setup

1. **Install Java 17 and Maven**
   ```bash
   # Verify installations
   java -version
   mvn -version
   ```

2. **Install VS Code Extensions**
   - Java Extension Pack (Microsoft)
   - Maven for Java (Microsoft)
   - Test Runner for Java (Microsoft)
   - Cucumber (Gherkin) Full Support (Alexander Kurakin) — for Cucumber only

3. **Open Project**
   ```bash
   code /path/to/PlayWrightWithJava
   ```

4. **Let VS Code index** — wait for Maven dependencies to resolve

## Running Tests

### From Terminal

```bash
# All tests
mvn test

# Specific framework setup
mvn test -Dtest=HomePageTest        # JUnit or TestNG
mvn test -Dtest=CucumberRunner      # Cucumber

# With options
mvn test -Dbrowser=firefox -Dheadless=false
```

### From VS Code Testing Panel

1. Open **Testing** sidebar (test beaker icon)
2. Expand test tree
3. Click play icon next to test class or individual test
4. View results in terminal

### From IDE (right-click in editor)

- Right-click test method → **Run**
- Right-click test method → **Debug**
- Right-click test class → **Run All Tests in Class**

---

# CI/CD

## GitHub Actions Workflow

**File:** `.github/workflows/ci-cd.yml`

```yaml
name: Test Automation

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main, develop]

jobs:
  test:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        browser: [chromium, firefox, webkit]

    steps:
      - uses: actions/checkout@v4

      - name: Set up Java 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven

      - name: Install Playwright browsers
        run: mvn exec:java -Dexec.mainClass="com.microsoft.playwright.CLI" -Dexec.args="install"

      - name: Run tests
        run: mvn test -Dbrowser=${{ matrix.browser }}

      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: test-results-${{ matrix.browser }}
          path: target/surefire-reports/

      - name: Upload screenshots
        if: failure()
        uses: actions/upload-artifact@v4
        with:
          name: screenshots-${{ matrix.browser }}
          path: test-results/screenshots/

      - name: Publish Allure Report
        if: always()
        uses: simple-elf/allure-report-action@master
        with:
          allure_results: target/allure-results
```

---

## Useful Commands

```bash
# Compilation
mvn clean compile
mvn clean test-compile

# Testing
mvn test
mvn test -Dtest=HomePageTest
mvn test -Dgroups=smoke                    # TestNG groups
mvn test -Dcucumber.filter.tags="@smoke"   # Cucumber tags

# Reporting
allure serve target/allure-results

# Dependencies
mvn versions:display-dependency-updates
mvn versions:display-plugin-updates
mvn dependency:tree

# Package
mvn -DskipTests package
```

---

## Quick Reference

| Need | Framework |
|------|-----------|
| Data-driven tests, retry logic, multiple suites | **TestNG** |
| Modern syntax, parameterized tests, simple flow | **JUnit 5** |
| BDD, stakeholder communication, scenarios | **Cucumber** |

---

## Support

For issues or questions:
- Check `target/surefire-reports/` for test failures
- Review `test-results/screenshots/` for failure screenshots
- Review logs in `target/` directory
- Check `.github/workflows/ci-cd.yml` for CI setup

---

# Cheat Sheet

Quick reference for annotations, assertions, and common patterns.

## TestNG Cheat Sheet

### Annotations

```java
@Test                           // Mark method as test
@Test(groups = "smoke")         // Add to group
@Test(enabled = false)          // Skip test
@Test(expectedExceptions = RuntimeException.class)  // Expect exception
@Test(timeoutMillis = 5000)     // Timeout in ms
@Test(retryAnalyzer = NetworkRetryAnalyzer.class)   // Retry on failure
@Test(dependsOnMethods = "setUp")                   // Depend on other test
@Test(dataProvider = "data")    // Use data provider
@Test(priority = 1)             // Execution order

@DataProvider(name = "data")
public Object[][] getData() {
    return new Object[][] { {"value1"}, {"value2"} };
}

@BeforeTest                     // Run before each test in class
@AfterTest                      // Run after each test in class
@BeforeClass                    // Run once before all tests
@AfterClass                     // Run once after all tests
@BeforeSuite                    // Run before entire suite
@AfterSuite                     // Run after entire suite
@BeforeMethod                   // Run before each method
@AfterMethod                    // Run after each method

@Listeners(ExtentReportExtension.class)             // Attach listeners
```

### Assertions

```java
assertTrue(condition)
assertFalse(condition)
assertEquals(expected, actual)
assertNotEquals(expected, actual)
assertNull(object)
assertNotNull(object)
assertThrows(Exception.class, () -> { /* code */ })
```

### Test Execution

```bash
# All tests
mvn test

# Specific test
mvn test -Dtest=HomePageTest

# Specific method
mvn test -Dtest=HomePageTest#shouldOpenPlaywrightHomePage

# By group
mvn test -Dgroups=smoke

# Multiple groups
mvn test -Dgroups=smoke,regression

# Exclude groups
mvn test -DexcludedGroups=slow

# Parallel execution
mvn test -Dparallel=methods -DthreadCount=8

# With retry
mvn test -Dretry=2
```

### Common Patterns

```java
// Data-driven test
@DataProvider(name = "browsers")
public Object[][] browsers() {
    return new Object[][] {
        {"chromium"},
        {"firefox"},
        {"webkit"}
    };
}

@Test(dataProvider = "browsers")
void testMultipleBrowsers(String browser) {
    setUpForBrowser(browser);
    // test code
}

// Retry on network failure
@Test(retryAnalyzer = NetworkRetryAnalyzer.class)
void testWithRetry() {
    // test code
}

// Test with timeout
@Test(timeoutMillis = 10000)
void testWithTimeout() {
    // test code
}

// Test with dependency
@Test(priority = 1)
void setup() { }

@Test(priority = 2, dependsOnMethods = "setup")
void runAfterSetup() { }
```

---

## JUnit 5 Cheat Sheet

### Annotations

```java
@Test                           // Mark method as test
@DisplayName("Test description")              // Human-readable name
@Tag("smoke")                   // Add tag
@Tag("smoke")
@Tag("regression")              // Multiple tags

@ParameterizedTest              // Data-driven test
@ValueSource(strings = {"chrome", "firefox"})
@CsvSource({ "a,1", "b,2" })
@MethodSource("dataProvider")
@ArgumentsSource(CustomArgumentsProvider.class)

@Disabled                       // Skip test
@Disabled("Not implemented yet")

@Timeout(value = 5, unit = ChronoUnit.SECONDS)  // Timeout

@ExtendWith(PlaywrightExtension.class)         // Use extension
@ExtendWith({Ext1.class, Ext2.class})          // Multiple extensions

@BeforeEach                     // Run before each test
@AfterEach                      // Run after each test
@BeforeAll                      // Run once before all tests
@AfterAll                       // Run once after all tests
@Nested                         // Nested test class

@TestInfo                       // Inject test info
@TestReporter                   // Inject reporter
```

### Assertions

```java
// Basic assertions
assertTrue(condition)
assertFalse(condition)
assertEquals(expected, actual)
assertNotEquals(expected, actual)
assertNull(object)
assertNotNull(object)
assertSame(expected, actual)
assertNotSame(expected, actual)

// Grouped assertions
assertAll(
    () -> assertEquals(1, 1),
    () -> assertEquals("a", "a")
)

// Exception assertions
assertThrows(Exception.class, () -> { /* code */ })
Exception exc = assertThrows(IOException.class, () -> {
    throw new IOException("error");
});
assertEquals("error", exc.getMessage());

// Timeout assertion
assertTimeout(Duration.ofSeconds(2), () -> {
    // code should complete within 2 seconds
})
```

### Test Execution

```bash
# All tests
mvn test

# Specific test
mvn test -Dtest=HomePageTest

# Specific method
mvn test -Dtest=HomePageTest#shouldOpenPlaywrightHomePage

# By tag
mvn test -Dgroups=smoke

# Include multiple tags
mvn test -Dgroups=smoke,regression

# Exclude tag
mvn test -DexcludedGroups=slow

# Parallel execution
mvn test -DthreadCount=8

# Display names in output
mvn test -Dplatform.reporting.open.mode=out
```

### Common Patterns

```java
// Parameterized test with ValueSource
@ParameterizedTest(name = "Browser: {0}")
@ValueSource(strings = {"chromium", "firefox", "webkit"})
void testMultipleBrowsers(String browser) {
    setUpForBrowser(browser);
    // test code
}

// Parameterized test with CsvSource
@ParameterizedTest
@CsvSource({
    "chromium, true",
    "firefox, true",
    "webkit, false"
})
void testWithCsv(String browser, boolean supported) {
    // test code
}

// Parameterized test with MethodSource
@ParameterizedTest
@MethodSource("provideData")
void testWithMethodSource(String data) {
    // test code
}

static Stream<Arguments> provideData() {
    return Stream.of(
        Arguments.of("chromium"),
        Arguments.of("firefox")
    );
}

// Test with extension
@ExtendWith(PlaywrightExtension.class)
@Tag("smoke")
@DisplayName("Home Page Tests")
class HomePageTest {
    @Test
    void shouldOpenPage() {
        // test code
    }
}

// Nested tests
@DisplayName("Home Page")
class HomePageTest {
    @Nested
    @DisplayName("Navigation")
    class NavigationTests {
        @Test
        void shouldNavigateToPage() { }
    }
}
```

---

## Cucumber Cheat Sheet

### Feature File Syntax

```gherkin
# Feature: description
Feature: User login
  As a user
  I want to login to the application
  So that I can access protected features

  Background:
    Given the application is open
    And I am on the login page

  @smoke @critical
  Scenario: Successful login
    Given I have valid credentials
    When I click the login button
    Then I should see the dashboard
    And the welcome message should display

  @regression
  Scenario Outline: Login with different users
    Given I enter "<username>" and "<password>"
    When I click login
    Then I should see "<result>"

    Examples:
      | username | password | result   |
      | user1    | pass1    | dashboard|
      | user2    | pass2    | dashboard|

  Scenario: Invalid login attempt
    Given I enter invalid credentials
    When I click login
    Then I should see an error message
```

### Step Definition Annotations

```java
// Given - precondition/setup
@Given("I am on the login page")
void stepGiven() { }

// When - action
@When("I click the login button")
void stepWhen() { }

// Then - assertion/verification
@Then("I should see the dashboard")
void stepThen() { }

// Before/After hooks
@Before
void beforeScenario(Scenario scenario) {
    System.out.println("Starting: " + scenario.getName());
}

@After
void afterScenario(Scenario scenario) {
    if (scenario.isFailed()) {
        // take screenshot
    }
}

@Before("@smoke")
void beforeSmokeTest() { }

@After("@smoke")
void afterSmokeTest() { }
```

### Step Parameters

```gherkin
Scenario: Enter username
  When I enter "john_doe" in username field
  Then I should see "john_doe" in the field
```

```java
@When("I enter {string} in username field")
void enterUsername(String username) {
    // use username
}

@Then("I should see {string} in the field")
void verifyText(String expectedText) {
    // verify expectedText
}
```

### Data Tables

```gherkin
Scenario: Create users
  Given I have the following users:
    | name  | role  | active |
    | Alice | admin | true   |
    | Bob   | user  | false  |
```

```java
@Given("I have the following users:")
void createUsers(DataTable dataTable) {
    List<Map<String, String>> users = dataTable.asMaps(String.class, String.class);
    for (Map<String, String> user : users) {
        String name = user.get("name");
        String role = user.get("role");
        boolean active = Boolean.parseBoolean(user.get("active"));
    }
}
```

### Test Execution

```bash
# All features
mvn test

# Run CucumberRunner
mvn test -Dtest=CucumberRunner

# By tag (@smoke)
mvn test -Dcucumber.filter.tags="@smoke"

# Multiple tags (OR)
mvn test -Dcucumber.filter.tags="@smoke or @regression"

# Multiple tags (AND)
mvn test -Dcucumber.filter.tags="@smoke and @critical"

# Exclude tags (NOT)
mvn test -Dcucumber.filter.tags="not @skip"

# By feature name
mvn test -Dcucumber.filter.name="Login"

# Dry run (parse only, no execute)
mvn test -Dcucumber.publish.enabled=false

# Generate report
allure serve target/allure-results
```

### Common Patterns

```gherkin
# Pattern 1: Scenario Outline with Examples
Scenario Outline: Test multiple browsers
  Given I am using <browser>
  When I navigate to home page
  Then I should see <result>

  Examples:
    | browser  | result |
    | chromium | logo   |
    | firefox  | logo   |

# Pattern 2: Multiple Given/When/Then
Scenario: Complex workflow
  Given I am logged in
  And I have 5 items in cart
  When I click checkout
  And I enter payment details
  And I confirm order
  Then I should see confirmation
  And I should receive email
  And order should be in database
```

```java
// Common step patterns
@Given("I am on the {word} page")
void navigateToPage(String pageName) {
    // navigate to page
}

@When("I click the {word} button")
void clickButton(String buttonName) {
    // click button
}

@Then("the {word} should be {word}")
void verifyElement(String element, String state) {
    // verify state
}

@And("I wait {int} seconds")
void waitSeconds(int seconds) {
    Thread.sleep(seconds * 1000);
}
```

### Hooks Best Practices

```java
public class Hooks {

    private static final Logger LOGGER = LoggerFactory.getLogger(Hooks.class);

    @Before(order = 1)
    public void setupBrowser(Scenario scenario) {
        LOGGER.info("Starting: {}", scenario.getName());
        PlaywrightManager.initialize();
    }

    @Before(value = "@slow", order = 2)
    public void setupSlowTest() {
        // Extra setup for slow tests
    }

    @After(order = 1)
    public void teardown(Scenario scenario) {
        if (scenario.isFailed()) {
            ScreenshotUtil.capture(PlaywrightManager.page(), scenario.getName());
            LOGGER.error("Failed: {}", scenario.getName());
        }
    }

    @After(order = 2)
    public void cleanup() {
        PlaywrightManager.shutdown();
    }
}
```

---

## Assertion Comparison

| Assertion | TestNG | JUnit 5 |
|-----------|--------|---------|
| True/False | `assertTrue()` `assertFalse()` | `assertTrue()` `assertFalse()` |
| Equal | `assertEquals()` | `assertEquals()` |
| Null | `assertNull()` `assertNotNull()` | `assertNull()` `assertNotNull()` |
| Exception | `assertThrows()` | `assertThrows()` |
| All | — | `assertAll()` |
| Timeout | `@Test(timeoutMillis=X)` | `assertTimeout()` |

---

## Framework Comparison

| Feature | TestNG | JUnit 5 | Cucumber |
|---------|--------|---------|----------|
| **Annotations** | `@Test`, `@DataProvider`, `@Listeners` | `@Test`, `@ParameterizedTest`, `@ExtendWith` | `@Given`, `@When`, `@Then`, `@Before`, `@After` |
| **Data-Driven** | DataProvider | @ParameterizedTest | Scenario Outline |
| **Groups** | `@Test(groups="smoke")` | `@Tag("smoke")` | `@smoke` tags |
| **Setup/Teardown** | `@BeforeMethod`, `@AfterMethod` | `@BeforeEach`, `@AfterEach` | `@Before`, `@After` |
| **Parallelization** | ✅ Built-in | ✅ With JUnit Platform | ✅ With runners |
| **Reports** | Extent Reports | Allure Reports | Allure BDD |
| **Learning Curve** | Medium | Low | Medium |
| **Best For** | Complex, data-driven | Modern, simple | BDD, stakeholders |

---

## Maven Commands by Framework

### TestNG

```bash
mvn test                                    # All tests
mvn test -Dgroups=smoke                    # By group
mvn test -Dtest=HomePageTest               # Specific class
mvn test -Dparallel=methods -DthreadCount=4  # Parallel
```

### JUnit 5

```bash
mvn test                                    # All tests
mvn test -Dgroups=smoke                    # By tag
mvn test -Dtest=HomePageTest               # Specific class
mvn test -DthreadCount=4                   # Parallel
```

### Cucumber

```bash
mvn test -Dtest=CucumberRunner             # All features
mvn test -Dcucumber.filter.tags="@smoke"   # By tag
mvn test -Dcucumber.filter.name="Login"    # By name
allure serve target/allure-results         # View report
```

---

## Quick Comparison Table

| Task | TestNG | JUnit 5 | Cucumber |
|------|--------|---------|----------|
| Run single test | `@Test` + `mvn test -Dtest=Class#method` | `@Test` + `mvn test -Dtest=Class#method` | `mvn test -Dcucumber.filter.name="Scenario"` |
| Skip test | `@Test(enabled=false)` | `@Disabled` | `@skip` tag |
| Set timeout | `@Test(timeoutMillis=5000)` | `@Timeout(5, ChronoUnit.SECONDS)` | `@timeout:5` hook |
| Data-driven | `@DataProvider` | `@ParameterizedTest` + `@ValueSource` | Scenario Outline + Examples |
| Retry | `retryAnalyzer` | Custom extension | Hooks + loop |
| Parallel run | `parallel=methods` `-DthreadCount=4` | `-DthreadCount=4` | Runner configuration |
| Reports | Extent Reports | Allure + JUnit | Allure BDD |

---

## Common Errors & Solutions

### TestNG

```java
// ❌ DataProvider not found
// Make sure @DataProvider name matches @Test(dataProvider="name")

// ✅ Correct
@DataProvider(name = "data")
public Object[][] getData() { }

@Test(dataProvider = "data")
void test(Object obj) { }
```

### JUnit 5

```java
// ❌ Extension not applied
// Make sure @ExtendWith is on class, not method

// ✅ Correct
@ExtendWith(PlaywrightExtension.class)
class MyTest {
    @Test
    void test() { }
}
```

### Cucumber

```gherkin
# ❌ Step not defined
# When I do something undefined
# Step not found - create implementation

# ✅ Correct
# When I click the button
@When("I click the button")
void clickButton() { }
```

---
