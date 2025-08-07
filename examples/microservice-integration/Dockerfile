# Example Dockerfile for a Java microservice with POM validation in build stage
# This shows how to integrate POM validation as part of your Docker build process

FROM maven:3.9-openjdk-21 AS build

# Set working directory
WORKDIR /app

# Copy POM file first for better layer caching
COPY pom.xml .

# Install POM Validator Tool
RUN curl -fsSL https://raw.githubusercontent.com/firefly-oss/pom-validator-tool/main/install.sh | bash && \
    export PATH="$HOME/.local/bin:$PATH"

# Validate POM structure before building
RUN $HOME/.local/bin/pom-validator pom.xml || (echo "❌ POM validation failed" && exit 1)

# Download dependencies (this layer will be cached if POM hasn't changed)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests && \
    mv target/*.jar app.jar

# Production stage
FROM openjdk:21-jre-slim AS runtime

# Install required packages for production
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd -r appuser && useradd --no-log-init -r -g appuser appuser

# Set working directory
WORKDIR /app

# Copy the JAR file from build stage
COPY --from=build /app/app.jar app.jar

# Change ownership to non-root user
RUN chown appuser:appuser app.jar

# Switch to non-root user
USER appuser

# Expose application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
