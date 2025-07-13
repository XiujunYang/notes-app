# NotesApp
A Spring Boot application for managing personal notes with RESTful API endpoints.

## Features

- Create, read, update and delete notes
- Retrieve word statistics from note
- MongoDB integration for data persistence
- Swagger/OpenAPI documentation
- Docker support for containerization
- Comprehensive test coverage with JUnit and TestContainers
- Modern Java 21 support

## Tech Stack

- Spring Boot 3.5.3
- MongoDB
- Java 21
- Lombok for code generation
- Springdoc OpenAPI for API documentation
- TestContainers for integration testing

## Prerequisites

- Java 21 JDK
- MongoDB (can be run via Docker)
- Gradle (optional, if not using the wrapper)
- Docker-compose (optional)

## Getting Started

### 1. Build and Run with Gradle

```bash
./gradlew clean bootRun
```

The application will be available at `http://localhost:8080`

### 2. Build and Run with Docker

```bash
docker build -t notes-app .
docker run -p 8080:8080 notes-app
```
or
```bash
docker-compose -f up -d
```

### 3. API Documentation

Access the Swagger UI at `http://localhost:8080/swagger-ui.html`

## Testing

Run the test suite with:

```bash
./gradlew test
```

Generate test coverage report:

```bash
./gradlew jacocoTestReport
```

The reports will be available in `build/reports/jacoco/test/html/`

## API Endpoints

- `POST /api/v1/notes` - Create a new note
- `GET /api/v1/notes` - List all notes with summary info
- `GET /api/v1/notes/{id}` - Fetch a single note
- `PUT /api/v1/notes/{id}` - Update a note
- `DELETE /api/v1/notes/{id}` - Delete a note
- `GET /api/v1/notes/{id}/stats` - Retrieve word statistics for a note
- `GET /api/v1/notes/tags` - List supported tags