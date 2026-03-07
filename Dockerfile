# =========================
# Stage 1: Build
# =========================
FROM eclipse-temurin:17-jre-jammy AS builder

WORKDIR /app

# Copy semua file
COPY . .

# Beri permission gradlew
RUN chmod +x ./gradlew

# Build fat jar
RUN ./gradlew shadowJar --no-daemon

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
ENTRYPOINT ["java","-XX:+UseContainerSupport","-XX:MaxRAMPercentage=75","-XX:+TieredCompilation","-XX:TieredStopAtLevel=1","-XX:+UseSerialGC","-Xshare:on","-jar","app.jar"]