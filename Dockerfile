# ===== BUILD STAGE =====
FROM eclipse-temurin:17-jdk-alpine as build

# Set working directory
WORKDIR /app

# Install Maven
RUN apk add --no-cache maven

# Copy pom.xml first for dependency caching
COPY demo/pom.xml ./

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY demo/src ./src

# Build application
RUN mvn clean package -DskipTests

# ===== PRODUCTION STAGE =====
FROM eclipse-temurin:17-jre-alpine

# Install curl for health checks
RUN apk add --no-cache curl

# Create a non-root user
RUN addgroup -S appuser && adduser -S appuser -G appuser

# Set working directory
WORKDIR /app

# Copy built jar
COPY --from=build /app/target/*.jar app.jar

# Change ownership
RUN chown appuser:appuser app.jar

# Switch to app user
USER appuser

# Expose port
EXPOSE 8080

# Unset problematic Hikari environment variable
ENV SPRING_DATASOURCE_HIKARI_POOL_NAME=

# Optional: JVM options for container
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
