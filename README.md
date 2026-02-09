# Ktor Locations API (APOC)

Ktor-based REST API for managing locations, including parking spaces and EV charging stations. Features robust geospatial search, JWT-based security with scope-based authorization, and standardized error handling.

## Features
- **CRUD Operations**: Full management of locations, parking spaces, and EV charging stations.
- **Geospatial Search**: Find locations within a specific radius using PostGIS.
- **Security**: JWT-based authentication with `locations:read` and `locations:write` scopes.
- **Validation**: Strict input validation for coordinates and data fields.
- **Standardized Errors**: Consistent JSON error responses for all API failures.
- **Modular Architecture**: Clean project structure under the `com.apoc` package.

## API Documentation
Interactive Swagger UI documentation is available via GitHub Pages:
[https://antonvolker.github.io/ktor-api/](https://antonvolker.github.io/ktor-api/)

## Prerequisites
- **PostgreSQL 17+** with the **PostGIS** extension installed.
- **JDK 21**.

## Setup & Run Locally

1. **Database Configuration**:
   The API expects a database named `ktor_api_db`. Ensure PostGIS is enabled in the database:
   ```sql
   CREATE EXTENSION postgis;
   ```

2. **Environment Variables**:
   Set the following variables (or use defaults for local testing):
   - `JDBC_DATABASE_URL`: `jdbc:postgresql://localhost:5432/ktor_api_db`
   - `DB_USER`: `postgres`
   - `DB_PASSWORD`: `postgres`
   - `JWT_SECRET`: Your signing secret (default: `secret`)
   - `JWT_ISSUER`: `http://0.0.0.0:8080/`
   - `JWT_AUDIENCE`: `apoc-api`

3. **Start the server**:
   ```bash
   ./gradlew run
   ```

## Security & Testing
Most endpoints require a Bearer JWT. 

- **Read Access**: Requires `locations:read` scope.
- **Write Access**: Requires `locations:write` scope.

To test locally with the default secret, you can generate a token at [jwt.io](https://jwt.io) using the HMAC256 algorithm and the payload:
```json
{
  "iss": "http://0.0.0.0:8080/",
  "aud": "apoc-api",
  "scope": "locations:read locations:write"
}
```

## Docker
Build and run the containerized API:
```bash
docker build -t ktor-api:latest .
docker run -p 8080:8080 ktor-api:latest
```