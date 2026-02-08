# Copilot instructions for this repo

## Big picture
- This is a single-module Ktor server app. The entry point is `main()` in [src/main/kotlin/com/example/Application.kt](../src/main/kotlin/com/example/Application.kt).
- The server uses Netty and exposes HTTP routes via `routing { ... }` inside `Application.configureRouting()`; this is currently the only route definition.
- JSON serialization is handled by Ktor ContentNegotiation with kotlinx.serialization (`@Serializable` models like `Member`).

## Key files and patterns
- Server bootstrap and plugins: [src/main/kotlin/com/example/Application.kt](../src/main/kotlin/com/example/Application.kt).
- Build configuration and dependencies: [build.gradle.kts](../build.gradle.kts).
- Project name: [settings.gradle.kts](../settings.gradle.kts).

## Developer workflows
- Run the server: `./gradlew run` (Ktor Gradle plugin is applied in [build.gradle.kts](../build.gradle.kts)).
- Build: `./gradlew build`.
- No test sources were found in this repo; add tests under `src/test/kotlin` if needed.

## Conventions specific to this repo
- Routes are defined in `configureRouting()` rather than inline in `main()`; keep new endpoints grouped there.
- JSON output is configured with `prettyPrint` and `isLenient`—keep consistent when adding serializers.
- Models intended for JSON responses should be `@Serializable` data classes.

## Integration points
- Uses Ktor server artifacts and Netty engine (`io.ktor:ktor-server-*-jvm`).
- Uses Logback for logging (`ch.qos.logback:logback-classic`).