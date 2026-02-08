# Build stage – use Gradle image so we don't need a wrapper in the repo
FROM gradle:8.11-jdk21-alpine AS build
WORKDIR /app

COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY src src

RUN gradle buildFatJar --no-daemon

# Run stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the fat JAR from build stage
COPY --from=build /app/build/libs/*-all.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
