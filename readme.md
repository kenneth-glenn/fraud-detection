# Fraud Detection Services Setup

This document explains how to set up the fraud detection services using the provided `docker-compose` file and how to modify the ports if the default ports (e.g., `8080` for the Spring Boot app, `5432` for Postgres) are occupied on your machine.

## Prerequisites
- Install Docker: [Get Docker](https://docs.docker.com/get-docker/)
- Install Docker Compose: [Install Docker Compose](https://docs.docker.com/compose/install/)

---

## Services Overview
The services defined in the `docker-compose.yml` file include:
- **Postgres Database**:
  Runs a PostgreSQL database instance.
- **Flyway**:
  Handles database migrations.
- **Spring Boot Application**:
  The main application container.

### Volumes:
The following volumes are defined:
- `postgres_data`: Stores persistent PostgreSQL data.
- `gradle_cache`: Caches Gradle files for the Spring Boot application.

---

## Running the Containers
1. Navigate to the directory where `docker-compose.yml` is located.
2. Run the following command to start the services:
   ```bash
   docker-compose up
   ```
   This will build and start the containers in the foreground.

3. To run them in the background (detached mode), use:
   ```bash
   docker-compose up -d
   ```

---

## Changing Default Ports

If any of the default ports are already being used (e.g., `8080` for Spring Boot or `5432` for Postgres), they can be changed in the `docker-compose.yml` file.

### Example: Change the Application Port (`8080`)
1. Locate this section for the Spring Boot container in `docker-compose.yml`:
   ```yaml
   ports:
     - "8080:8080"
   ```
2. Change the host port (the first number) to any available port. Example:
   ```yaml
   ports:
     - "8081:8080"
   ```
   With this setup:
    - The application inside the container remains accessible on port `8080`.
    - On your host machine, you access it via port `8081`.

---

### Example: Change the Database Port (`5432`)
1. Locate this section for the Postgres container:
   ```yaml
   ports:
     - "5432:5432"
   ```
2. Change the host port (the first number) to any available port. Example:
   ```yaml
   ports:
     - "5433:5432"
   ```
   With this setup:
    - Inside the container, the database is accessible on port `5432`.
    - On your host machine, it will be accessible on port `5433`.

**Note:** If you change the Postgres host port, ensure that other services depending on this database (e.g., Flyway) are updated accordingly:
```yaml
flyway:
  command:
    - -url=jdbc:postgresql://fraud_detection_db:5433/fraud_detection
```

---

## Stopping the Containers
To stop the running containers:
```bash
docker-compose down
```

---

## Testing

- Unit tests exist in frauddetection/src/test/java/org/fiverty/frauddetection/service
  - Basic set of tests to test the service layer. 
- Postman examples exist in frauddetection/postman
  - Postman provides the ability to test Requests

---

## Troubleshooting
- **Conflict with default ports**: If the services fail to start due to port conflicts, refer to the instructions above to modify the ports.
- **Expose Ports Properly**: Ensure the ports section correctly maps the host and container ports. Refer to [IntelliJ Documentation](https://www.jetbrains.com/help/idea/2024.2/docker-compose.html) for additional configuration tips.

---

## References
- Docker Compose Documentation: [https://docs.docker.com/compose/](https://docs.docker.com/compose/)
- Postgres Docker Image Details: [https://hub.docker.com/_/postgres](https://hub.docker.com/_/postgres)
- Flyway Docker Image Details: [https://hub.docker.com/r/flyway/flyway](https://hub.docker.com/r/flyway/flyway)
- Gradle Docker Image Details: [https://hub.docker.com/r/gradle/gradle](https://hub.docker.com/r/gradle/gradle)
- Helpful IntelliJ Docs: [Multi-Container Applications in Docker](https://www.jetbrains.com/help/idea/2024.2/docker-compose.html) [[1]](https://www.jetbrains.com/help/idea/2024.2/dockerfile-run-configuration.html).