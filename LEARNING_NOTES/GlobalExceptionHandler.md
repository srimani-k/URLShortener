# Global Exception Handling

## Problem

Before implementing exception handling, errors were handled manually inside controllers or resulted in generic server errors.

Example:

```java
if(urlRequestDTO.getUrl() == null ||
   urlRequestDTO.getUrl().isBlank()) {

    return ResponseEntity.status(400).body(null);
}
```

This works, but validation logic starts spreading across controllers.

Also:

```java
.orElseThrow(() ->
    new RuntimeException("Short URL not found"));
```

would result in a generic 500 Internal Server Error.

---

# Goal

Centralize error handling in one place and return meaningful HTTP status codes.

---

# What is an Exception?

An exception represents an unexpected situation.

Examples:

* URL not found
* Invalid URL input
* Database connection failure
* Null values

Instead of continuing execution, Java stops the current flow and throws an exception.

---

# Custom Exceptions

## ShortUrlNotFoundException

```java
public class ShortUrlNotFoundException
        extends RuntimeException {

    public ShortUrlNotFoundException(String message) {
        super(message);
    }
}
```

### Purpose

Thrown when a short code does not exist in the database.

Example:

```java
throw new ShortUrlNotFoundException(
        "Short URL not found");
```

---

## InvalidUrlException

```java
public class InvalidUrlException
        extends RuntimeException {

    public InvalidUrlException(String message) {
        super(message);
    }
}
```

### Purpose

Thrown when user sends an invalid URL.

Example:

```java
if(inputUrl == null ||
   inputUrl.isBlank()) {

    throw new InvalidUrlException(
            "URL cannot be empty");
}
```

---

# Why Custom Exceptions?

Instead of:

```java
RuntimeException
```

Use:

```java
ShortUrlNotFoundException
InvalidUrlException
```

Benefits:

* More readable
* Easier debugging
* Business errors become explicit
* Different exceptions can return different HTTP responses

---

# Global Exception Handler

Created:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
}
```

---

## @RestControllerAdvice

Meaning:

```text
Handle exceptions for all controllers
from one central location.
```

Without it:

```text
Controller 1 -> try/catch
Controller 2 -> try/catch
Controller 3 -> try/catch
```

With it:

```text
All Controllers
       ↓
GlobalExceptionHandler
```

---

# @ExceptionHandler

Used to handle specific exceptions.

Example:

```java
@ExceptionHandler(
    ShortUrlNotFoundException.class)
```

Meaning:

```text
Whenever this exception occurs,
execute this method.
```

---

# Handling ShortUrlNotFoundException

```java
@ExceptionHandler(
        ShortUrlNotFoundException.class)
public ResponseEntity<String>
handleShortUrlNotFoundException(
        ShortUrlNotFoundException ex){

    return ResponseEntity
            .status(404)
            .body(ex.getMessage());
}
```

---

## HTTP Status Learned

### 404 Not Found

Meaning:

```text
Requested resource does not exist.
```

Example:

```http
GET /xyz123
```

If short code is not found:

```http
404 Not Found
```

Response:

```text
Short URL not found
```

---

# Handling InvalidUrlException

```java
@ExceptionHandler(
        InvalidUrlException.class)
public ResponseEntity<String>
handleInvalidUrlException(
        InvalidUrlException ex){

    return ResponseEntity
            .status(400)
            .body(ex.getMessage());
}
```

---

## HTTP Status Learned

### 400 Bad Request

Meaning:

```text
Client sent invalid data.
```

Example:

```json
{
  "url":""
}
```

Response:

```http
400 Bad Request
```

Body:

```text
URL cannot be empty
```

---

# Validation Moved to Service Layer

Before:

```text
Controller
      ↓
Validation
      ↓
Service
```

After:

```text
Controller
      ↓
Service
      ↓
Validation
      ↓
Exception
```

Benefit:

Business rules stay in the Service layer.

Controllers remain clean.

---

# Exception Flow

## URL Not Found

```text
GET /abc123
        ↓
Controller
        ↓
Service
        ↓
Repository
        ↓
No Record Found
        ↓
Throw ShortUrlNotFoundException
        ↓
GlobalExceptionHandler
        ↓
404 Not Found
```

---

## Invalid URL

```text
POST /shorten
        ↓
Controller
        ↓
Service
        ↓
URL is empty
        ↓
Throw InvalidUrlException
        ↓
GlobalExceptionHandler
        ↓
400 Bad Request
```

---

# Concepts Learned

* Exception Handling
* RuntimeException
* Custom Exceptions
* @RestControllerAdvice
* @ExceptionHandler
* Centralized Error Handling
* Service Layer Validation
* HTTP 400 Bad Request
* HTTP 404 Not Found
* ResponseEntity

---

# Key Takeaway

Instead of handling errors inside every controller:

```text
Controller
      ↓
Throw Exception
      ↓
GlobalExceptionHandler
      ↓
Return Proper HTTP Response
```

This keeps controllers clean and provides consistent error responses across the entire application.
