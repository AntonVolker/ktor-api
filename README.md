# Ktor Locations API

Ktor-based REST API for managing locations, including parking spaces and EV charging stations. Built with Kotlin, Exposed ORM, PostgreSQL, and HikariCP.

## Features
- CRUD endpoints for locations
- Specialized create and update flows for parking spaces and EV charging stations
- JSON serialization with kotlinx.serialization
- Dockerfile for containerized builds

## API Docs
Swagger UI is hosted via GitHub Pages from the docs folder.

1. In your GitHub repo, go to Settings -> Pages.
2. Set Source to "Deploy from a branch".
3. Select Branch: main and Folder: /docs.
4. Save and wait for the build to finish.

Your docs will be available at:
https://<your-username>.github.io/<repo-name>/

## Run Locally
1. Ensure PostgreSQL is running and environment variables are set (or use defaults):
   - JDBC_DATABASE_URL (default: jdbc:postgresql://localhost:5432/ktor_api_db)
   - DB_USER (default: postgres)
   - DB_PASSWORD (default: postgres)
2. Start the server:
   ./gradlew run

## Docker
Build and run:

docker build -t ktor-api:latest .
docker run -p 8080:8080 ktor-api:latest

## License
Add a LICENSE file for your own code and keep THIRD_PARTY_NOTICES.md up to date.
