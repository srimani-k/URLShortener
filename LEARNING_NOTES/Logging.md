# Logging (SLF4J + Logback)

## Why Logging?

Logging helps monitor and debug applications, especially in production where `System.out.println()` is not practical.

Logs answer questions like:

* Did the request reach the service?
* Was a duplicate URL found?
* Was a short code generated?
* Was there a collision?
* Was the URL saved successfully?
* Why did a request fail?

---

# System.out.println() vs Logging

### System.out.println()

* Used for learning and quick debugging.
* No log levels.
* Difficult to filter.
* No timestamps or structured output.

Example:

```java
System.out.println("Inside service");
```

---

### Logging

Provides:

* Log levels
* Timestamps
* Thread information
* Easy filtering
* Production-ready monitoring

Example:

```java
log.info("Received request to shorten URL: {}", inputUrl);
```

---

# Spring Boot Logging

Spring Boot includes logging by default.

* **SLF4J** → Logging API
* **Logback** → Default logging implementation

No additional dependency is required.

---

# @Slf4j

Instead of creating a logger manually:

```java
private static final Logger logger =
        LoggerFactory.getLogger(UrlService.class);
```

Lombok provides:

```java
@Slf4j
```

which automatically generates:

```java
private static final Logger log = ...
```

---

# Log Levels

## TRACE

Very detailed logs.

Used rarely.

---

## DEBUG

Used during development.

Example:

```java
log.debug("Generated short code: {}", shortCode);
```

---

## INFO

Represents normal business events.

Examples:

* Request received
* URL created
* Redirect successful
* Statistics fetched

---

## WARN

Unexpected situations that the application can recover from.

Examples:

* Short code collision
* Expired URL
* URL not found

---

## ERROR

Application failures.

Examples:

* Database unavailable
* Unexpected exceptions

---

# Parameterized Logging

Preferred:

```java
log.info("Short code generated: {}", shortCode);
```

Avoid:

```java
log.info("Short code generated: " + shortCode);
```

Advantages:

* Better performance
* Cleaner code
* Standard SLF4J practice

---

# Logging Added in Our Project

## URL Creation

```java
log.info("Received request to shorten URL");
```

---

## Duplicate URL

```java
log.info("URL already exists. Returning existing URL.");
```

---

## Short Code Generation

```java
log.debug("Generated short code: {}", shortCode);
```

---

## Collision Detection

```java
log.warn("Collision detected for short code: {}", shortCode);
```

---

## URL Saved

```java
log.info("Short URL saved successfully with ID: {}", id);
```

---

## Redirect Request

```java
log.info("Redirect request received for short code: {}", shortCode);
```

---

## Expired URL

```java
log.warn("Expired URL accessed. Short code: {}", shortCode);
```

---

## Click Count

```java
log.info("Click count incremented to {} for short code {}", clickCount, shortCode);
```

---

## URL Statistics

```java
log.info("Fetching statistics for short code: {}", shortCode);
```

---

# Logging Best Practices

* Log business events, not every line of code.
* Use appropriate log levels.
* Use `{}` placeholders instead of string concatenation.
* Log before throwing expected exceptions.
* Do not log sensitive information such as passwords, tokens, or API keys.
* Keep log messages clear and meaningful.

---

# Key Takeaways

* Spring Boot uses SLF4J with Logback by default.
* `@Slf4j` automatically creates a logger.
* Logging is essential for debugging and monitoring production applications.
* INFO, DEBUG, WARN, and ERROR each serve different purposes.
* Parameterized logging (`{}`) is preferred over string concatenation.
* Good logging tells the story of a request from start to finish.
