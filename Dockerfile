# syntax=docker/dockerfile:1.7
#
# Reproducible local + CI runner. Uses Microsoft's official Playwright Java image
# which ships JDK 17, Maven, Node.js, and all three browsers (chromium/firefox/webkit)
# with their OS-level dependencies preinstalled.
#
# Build:
#   docker build -t playwright-java-framework .
#
# Run smoke on chromium (default):
#   docker run --rm -v "$PWD/test-results:/app/test-results" playwright-java-framework
#
# Run a different suite/browser:
#   docker run --rm -v "$PWD/test-results:/app/test-results" playwright-java-framework \
#       mvn -B -ntp test -Pregression -Dbrowsers=firefox
#
# Interactive shell:
#   docker run --rm -it --entrypoint bash playwright-java-framework

ARG PLAYWRIGHT_IMAGE=mcr.microsoft.com/playwright/java:v1.54.0-jammy

# -------- Stage 1: warm the Maven dependency cache -----------------------------
FROM ${PLAYWRIGHT_IMAGE} AS deps
WORKDIR /build
COPY pom.xml ./
COPY .mvn ./.mvn
RUN mvn -B -ntp -q -DskipTests dependency:go-offline || true

# -------- Stage 2: runtime image with sources ---------------------------------
FROM ${PLAYWRIGHT_IMAGE}
WORKDIR /app

# Reuse the primed local repo so subsequent builds are fast and offline-friendly.
COPY --from=deps /root/.m2 /root/.m2

# Copy project sources. .dockerignore excludes build output & secrets.
COPY . .

# Runtime knobs — override at `docker run` with -e.
ENV MAVEN_OPTS="-Dorg.slf4j.simpleLogger.defaultLogLevel=info" \
    HEADLESS=true \
    BROWSERS=chromium

# Default command runs the smoke suite headless on chromium.
# Override with any `mvn ...` invocation.
CMD ["sh", "-c", "mvn -B -ntp test -Psmoke -Dheadless=${HEADLESS} -Dbrowsers=${BROWSERS}"]
