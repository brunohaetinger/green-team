FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copy Gradle wrapper and build descriptors first for better layer caching.
COPY gradlew gradlew
COPY gradlew.bat gradlew.bat
COPY build.gradle settings.gradle ./
COPY gradle/ gradle/

# Copy application sources.
COPY src/ src/

# Build an executable Spring Boot jar.
RUN chmod +x gradlew && ./gradlew clean bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"]