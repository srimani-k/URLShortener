# Standardized API Error Responses

## Objective

Return all API errors in a consistent JSON format instead of plain text messages.

---

# Why Standardize Error Responses?

Earlier, our exception handlers returned only a String.

Example:

```text
Short URL not found
```

or

```text
URL cannot be empty
```

Although this works, it is not considered a good REST API design.

Clients (Frontend, Mobile Apps, Other Services) prefer structured JSON responses.

---

# Solution

Create a dedicated DTO.

```java
public class ErrorResponseDTO {

    private LocalDateTime timestamp;
    private int status;
    private String message;

}
```

This DTO represents the body of every error response.

---

# Error Response Format

Example:

```json
{
    "timestamp": "2026-06-28T10:30:15",
    "status": 404,
    "message": "Short URL not found"
}
```

Every exception now returns the same JSON structure.

---

# Updating GlobalExceptionHandler

Instead of:

```java
return ResponseEntity.status(404).body(ex.getMessage());
```

we create an ErrorResponseDTO.

```java
ErrorResponseDTO error = new ErrorResponseDTO(
        LocalDateTime.now(),
        HttpStatus.NOT_FOUND.value(),
        ex.getMessage()
);

return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
```

This pattern is used for all exception handlers.

---

# ResponseEntity vs Returning DTO Directly

## Returning DTO

```java
return error;
```

Only returns the response body.

Spring decides the HTTP status unless specified separately.

---

## Returning ResponseEntity

```java
return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
```

Allows complete control over:

* HTTP Status Code
* Response Headers
* Response Body

This is the preferred approach for REST APIs.

---

# Why Use HttpStatus Enum?

Instead of hardcoding numbers:

```java
404
400
410
```

Spring provides readable constants.

```java
HttpStatus.NOT_FOUND
HttpStatus.BAD_REQUEST
HttpStatus.GONE
```

To obtain the numeric value:

```java
HttpStatus.NOT_FOUND.value()
```

returns

```text
404
```

Advantages:

* More readable
* No magic numbers
* Easier to maintain

---

# Current Error Responses

Validation Error

```json
{
    "timestamp": "...",
    "status": 400,
    "message": "URL cannot be empty"
}
```

Short URL Not Found

```json
{
    "timestamp": "...",
    "status": 404,
    "message": "Short URL not found"
}
```

Expired URL

```json
{
    "timestamp": "...",
    "status": 410,
    "message": "URL has expired"
}
```

---

# Benefits

* Consistent API responses
* Easier frontend integration
* Better debugging
* Professional REST API design
* Reusable across all exceptions

---

# Concepts Learned

* ErrorResponseDTO
* Standardized API Responses
* ResponseEntity
* HttpStatus Enum
* REST API Best Practices
* JSON Error Responses

---

# Must Memorize (Interview)

✅ ResponseEntity controls:

* Status Code
* Headers
* Body

✅ Error DTOs are used to standardize API responses.

✅ Prefer:

```java
HttpStatus.NOT_FOUND
```

instead of

```java
404
```

Use:

```java
HttpStatus.NOT_FOUND.value()
```

when the numeric status code is required.

---

# Key Takeaways

* Success responses and error responses should both use DTOs.
* Keep all API errors in a consistent JSON format.
* ResponseEntity provides full control over the HTTP response.
* Using HttpStatus enums makes code more readable and maintainable.
