# =========================
# Stage 1: Build
# =========================
FROM gradle:8.7-jdk17 AS builder

WORKDIR /app

# Cache dependency layer
COPY build.gradle.kts settings.gradle.kts gradle.properties* ./
COPY gradle ./gradle
RUN gradle dependencies --no-daemon

# Copy source
COPY src ./src

# Build fat jar
RUN gradle shadowJar --no-daemon

# Generate CDS archive (improves startup)
RUN java -Xshare:dump -jar build/libs/crypto-monitor.jar || true

# =========================
# Stage 2: Runtime
# =========================
FROM gcr.io/distroless/java17-debian12

WORKDIR /app

COPY --from=builder /app/build/libs/crypto-monitor.jar app.jar

# Cloud-friendly port
ENV PORT=8080

EXPOSE 8080

# Run as non-root (security best practice)
USER nonroot

# JVM optimized for fast startup
ENTRYPOINT [
  "java",
  "-XX:+UseContainerSupport",
  "-XX:MaxRAMPercentage=75",
  "-XX:+TieredCompilation",
  "-XX:TieredStopAtLevel=1",
  "-XX:+UseSerialGC",
  "-Xshare:on",
  "-jar",
  "app.jar"
]