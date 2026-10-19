# URL Shortener --- Dockerization Notes

**Date:** 19 October 2026

## Goal

Containerize the Spring Boot URL Shortener and run the application,
MySQL, and Redis together locally using Docker Compose.

## What we did

### 1. Verified the project and built the JAR

Project directory:

``` bash
cd ~/Downloads/URLShortener
```

Built the application:

``` bash
./mvnw clean package -DskipTests
```

Result: `BUILD SUCCESS`

Generated JAR:

``` text
target/URLShortener-0.0.1-SNAPSHOT.jar
```

### 2. Created a Docker image

Created a `Dockerfile` in the project root:

``` dockerfile
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/URLShortener-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

Built the image:

``` bash
docker build -t urlshortener:1.0 .
```

Result: image `urlshortener:1.0` built successfully.

**What the Dockerfile does** - `FROM`: uses a Java 21 runtime image. -
`WORKDIR`: sets `/app` as the working directory in the container. -
`COPY`: copies the built JAR into the image. - `EXPOSE`: documents that
the application listens on port 8080. - `ENTRYPOINT`: starts the Spring
Boot application.

Note: This Dockerfile uses a prebuilt JAR. Rebuild the JAR before
rebuilding the image after code changes.

### 3. Configured environment-based application properties

`src/main/resources/application.properties`:

``` properties
spring.application.name=URLShortener

# MySQL
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/urlShortener}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# JPA
spring.jpa.show-sql=${SHOW_SQL:false}
spring.jpa.hibernate.ddl-auto=${DDL_AUTO:update}

# Redis
spring.data.redis.host=${REDIS_HOST:localhost}
spring.data.redis.port=${REDIS_PORT:6379}
```

This allows database credentials and service hosts to be configured
through environment variables rather than hardcoded in the properties
file.

### 4. Added Docker Compose

Created `compose.yaml` in the project root to define three services:

-   `app`: Spring Boot API, exposed on host port `8080`.
-   `mysql`: MySQL 8.4 database.
-   `redis`: Redis 7 Alpine cache.

The Compose setup uses: - `mysql` as the database hostname from the
application container. - `redis` as the Redis hostname from the
application container. - Health checks for MySQL and Redis. -
`depends_on` health conditions so the app waits for dependencies to
become healthy. - A named `mysql_data` volume for database
persistence. - No published host ports for MySQL or Redis.

The app uses these container environment values:

``` text
DB_URL=jdbc:mysql://mysql:3306/urlShortener
REDIS_HOST=redis
REDIS_PORT=6379
```

Inside a container, `localhost` means that same container. Compose
service names let the app reach the other containers over the Compose
network.

### 5. Added local secrets and Docker build exclusions

Created a local `.env` file with these variable names:

``` dotenv
DB_USERNAME=urlshortener
DB_PASSWORD=<your-local-database-password>
MYSQL_ROOT_PASSWORD=<a-different-local-root-password>
```

The angle-bracket values above are placeholders, not literal values.
Keep real credentials private and never commit `.env`.

-   Added `.env` to `.gitignore` if it was not already ignored.
-   Created `.dockerignore` to exclude Git metadata, IDE files, and
    `.env` from the Docker build context while retaining the required
    JAR:

``` text
.git
.idea
.env
target/*
!target/URLShortener-0.0.1-SNAPSHOT.jar
```

### 6. Validated and started the services

Validated the Compose configuration:

``` bash
docker compose config
```

Started the services:

``` bash
docker compose up --build -d
```

Checked service status:

``` bash
docker compose ps
```

Observed: - Spring Boot app: running, port `8080` published. - MySQL:
healthy. - Redis: healthy.

### 7. Tested the API

Checked the application logs:

``` bash
docker compose logs --tail=80 app
```

Confirmed the logs included:

``` text
Tomcat started on port 8080
Started UrlShortenerApplication
```

Requested `/`:

``` bash
curl -i http://localhost:8080/
```

Received `404 Not Found`. This is expected because the application does
not define a homepage route at `/`; it does not indicate that the server
failed to start.

Created a short URL:

``` bash
curl -i -X POST http://localhost:8080/shorten \
  -H "Content-Type: application/json" \
  -d '{"url":"https://www.example.com"}'
```

Result: `201 Created`, with a response like:

``` json
{
  "shortenUrl": "http://localhost:8080/d9d813"
}
```

The generated short code in the test was `d9d813`.

Tested the redirect:

``` bash
curl -i http://localhost:8080/d9d813
```

Result: `302 Found`, with:

``` http
Location: https://www.example.com
```

Restarted only the application container:

``` bash
docker compose restart app
```

Repeated the redirect request and received `302 Found` again. This is
evidence that the mapping remained available after an application
restart. Directly querying MySQL to inspect the record was not completed
yet.

## Current architecture

``` text
Client
  |
  | HTTP :8080
  v
Spring Boot app container
  |                 |
  |                 |
  v                 v
MySQL container    Redis container
  |
  v
Named volume: mysql_data
```

## Key concepts learned

-   **JAR:** the packaged Spring Boot application.
-   **Docker image:** the template used to create containers.
-   **Container:** a running instance of an image.
-   **Docker Compose:** defines and runs multiple related services
    together.
-   **Container networking:** services communicate using Compose service
    names such as `mysql` and `redis`, rather than `localhost`.
-   **Persistent volume:** keeps MySQL data outside the lifecycle of the
    MySQL container.
-   **Health checks:** help determine when MySQL and Redis are ready.
-   **HTTP 201:** the API created a shortened URL.
-   **HTTP 302:** the short URL endpoint redirected to the original URL.

## Useful commands

``` bash
# Check services
docker compose ps

# View application logs
docker compose logs --tail=80 app

# Follow application logs
docker compose logs -f app

# Restart only the app
docker compose restart app

# Stop and remove containers/networks while preserving named volumes
docker compose down
```

Avoid `docker compose down -v` unless you intentionally want to remove
the named volumes and their stored database data.

## Next steps (not completed yet)

1.  Directly verify the URL mapping in MySQL.
2.  Test the `/stats/{shortCode}` endpoint.
3.  Review the current local security configuration before any public
    deployment. The temporary `permitAll()` configuration discussed
    during local testing is not appropriate for production.
4.  Decide on a cloud deployment target and check its current cost
    before deploying.
5.  Plan a safer production configuration for secrets, database, Redis,
    logging, and health monitoring.

## End-of-session status

**Completed:** Docker image build, Compose setup, app/MySQL/Redis
startup, URL creation API test, redirect test, and application restart
test.

**Not yet completed:** direct SQL verification, stats endpoint test, and
cloud deployment.
