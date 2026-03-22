# Multi-stage Dockerfile for Kafka Connect Lineage Emitter
FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

# Copy Gradle wrapper and build files
COPY gradlew gradlew
COPY build.gradle.kts build.gradle.kts
COPY settings.gradle.kts settings.gradle.kts
COPY gradle/ gradle/

# Copy source code
COPY src/ src/

# Build the application
RUN ./gradlew clean shadowJar --no-daemon

# Runtime image
FROM eclipse-temurin:25-jre AS runtime
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/build/libs/kafka-connect-lineage-emitter-1.0-SNAPSHOT-fat.jar app-fat.jar

ENTRYPOINT ["java", "-jar", "app-fat.jar"]
