# ── Stage 1: build ─────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copy Maven wrapper and POM first for layer-cache efficiency
COPY pom.xml ./
# Download dependencies (cached unless pom.xml changes)
RUN mvn -B dependency:go-offline -q

COPY src ./src
RUN mvn -B package -DskipTests -q

# ── Stage 2: runtime ────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Non-root user for security
RUN addgroup -S devtrack && adduser -S devtrack -G devtrack
USER devtrack

COPY --from=builder /app/target/devtrack-backend-*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
