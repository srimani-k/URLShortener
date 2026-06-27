# URL Shortener - Learning Notes

## Project Goal

Build a URL Shortener using Spring Boot and MySQL.

Example:

Original URL:
https://google.com

Short URL:
http://localhost:8080/ab12cd

When a user visits the short URL, the application redirects them to the original URL.

---

# Project Architecture

```text
Client
  ↓
Controller
  ↓
Service
  ↓
Repository
  ↓
MySQL Database
```

### Controller

Handles HTTP requests and responses.

### Service

Contains business logic.

### Repository

Handles database operations.

### Database

Stores URL mappings.

---

# Database Entity

## UrlMappingEntity

```java
@Entity
public class UrlMappingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originalUrl;
    private String shortCode;
}
```

### Purpose

Stores mapping between:

```text
shortCode → originalUrl
```

Example:

```text
ab12cd → https://google.com
```

---

# DTOs (Data Transfer Objects)

## UrlRequestDTO

```java
public class UrlRequestDTO {
    private String url;
}
```

### Purpose

Receives request data from client.

Example Request:

```json
{
  "url":"https://google.com"
}
```

---

## UrlResponseDTO

```java
public class UrlResponseDTO {
    private String shortenUrl;
}
```

### Purpose

Sends response data back to client.

Example Response:

```json
{
  "shortenUrl":"http://localhost:8080/ab12cd"
}
```

---

# Repository Layer

## UrlRepository

```java
@Repository
public interface UrlRepository
        extends JpaRepository<UrlMappingEntity, Long> {

    Optional<UrlMappingEntity> findByShortCode(String shortCode);
}
```

### Concepts Learned

### JpaRepository

Provides built-in methods:

```java
save()
findById()
findAll()
delete()
```

No SQL required.

---

### Custom Query Method

```java
findByShortCode(String shortCode)
```

Spring automatically creates query:

```sql
SELECT *
FROM url_mapping_entity
WHERE short_code = ?
```

---

### Optional

Return type:

```java
Optional<UrlMappingEntity>
```

Purpose:

Handle "record not found" safely.

Example:

```java
Optional<UrlMappingEntity> url
```

May contain:

```text
UrlMappingEntity
```

or

```text
empty
```

---

# Service Layer

## Purpose

Contains business logic for:

1. Creating short URLs
2. Retrieving original URLs

---

# URL Shortening Flow

Method:

```java
postShortenUrl(String inputUrl)
```

Flow:

```text
Receive original URL
        ↓
Generate short code
        ↓
Create entity
        ↓
Save to database
        ↓
Return shortened URL
```

---

## Generating Short Code

Code:

```java
String generateShortCode =
    UUID.randomUUID().toString().substring(0, 6);
```

### UUID

Example:

```text
550e8400-e29b-41d4-a716-446655440000
```

### substring(0,6)

Extracts first 6 characters.

Example:

```text
550e84
```

Used as short code.

---

## Saving Data

```java
UrlMappingEntity urlBody =
        new UrlMappingEntity();

urlBody.setShortCode(generateShortCode);
urlBody.setOriginalUrl(inputUrl);

urlRepository.save(urlBody);
```

### Result

Database stores:

| id | shortCode | originalUrl        |
| -- | --------- | ------------------ |
| 1  | ab12cd    | https://google.com |

---

## Creating Response

```java
UrlResponseDTO response =
        new UrlResponseDTO();

response.setShortenUrl(
    "http://localhost:8080/"
    + urlBody.getShortCode()
);
```

Response:

```json
{
  "shortenUrl":"http://localhost:8080/ab12cd"
}
```

---

# Redirect Flow

Method:

```java
getOriginalUrlFromShortenUrl(
    String shortenCode
)
```

Flow:

```text
Receive shortCode
        ↓
Search database
        ↓
Find original URL
        ↓
Return original URL
```

---

## Finding URL

```java
UrlMappingEntity url =
    urlRepository
      .findByShortCode(shortenCode)
      .orElseThrow(
         () -> new RuntimeException(
             "Short URL not found"
         )
      );
```

### orElseThrow()

If record exists:

```text
Return UrlMappingEntity
```

If record does not exist:

```text
Throw Exception
```

---

## Returning Original URL

```java
return url.getOriginalUrl();
```

Example:

```text
https://google.com
```

---

# Controller Layer

## POST /shorten

Endpoint:

```java
@PostMapping("/shorten")
```

Request:

```http
POST /shorten
```

Body:

```json
{
  "url":"https://google.com"
}
```

---

## Input Validation

```java
if(urlRequestDTO.getUrl() == null
   || urlRequestDTO.getUrl().isBlank())
```

Purpose:

Reject invalid URLs.

Response:

```java
return ResponseEntity
       .status(400)
       .body(null);
```

---

### HTTP 400

```text
400 Bad Request
```

Meaning:

Client sent invalid data.

---

## Successful Response

```java
return ResponseEntity
       .status(201)
       .body(response);
```

### HTTP 201

```text
201 Created
```

Meaning:

New resource successfully created.

---

# GET /{shortenCode}

Endpoint:

```java
@GetMapping("/{shortenCode}")
```

Example:

```http
GET /ab12cd
```

Flow:

```text
Browser requests short URL
          ↓
Controller receives shortCode
          ↓
Service finds original URL
          ↓
Controller returns redirect
          ↓
Browser opens original URL
```

---

# ResponseEntity

Used to build custom HTTP responses.

Example:

```java
ResponseEntity.status(201)
              .body(response);
```

---

# Redirect Response

Code:

```java
return ResponseEntity
        .status(302)
        .header(
           "Location",
           originalUrl
        )
        .build();
```

---

## HTTP 302

```text
302 Found
```

Meaning:

Resource exists somewhere else.

Browser should navigate to new location.

---

## Location Header

```java
.header(
   "Location",
   originalUrl
)
```

Adds:

```http
Location: https://google.com
```

Browser reads this header and automatically redirects.

---

## build()

```java
.build()
```

Creates response without body.

Reason:

Redirect responses do not need JSON data.

---

# End-to-End Flow

## URL Creation

```text
POST /shorten
        ↓
Receive URL
        ↓
Generate short code
        ↓
Save mapping in DB
        ↓
Return short URL
```

Example:

```text
https://google.com

↓

http://localhost:8080/ab12cd
```

---

## URL Redirection

```text
GET /ab12cd
        ↓
Find row using shortCode
        ↓
Get originalUrl
        ↓
Return HTTP 302
        ↓
Browser redirects
```

---

# Concepts Learned

* Spring Boot
* REST APIs
* Controller Layer
* Service Layer
* Repository Layer
* MySQL
* JPA
* Hibernate
* Entity Mapping
* DTO Pattern
* UUID
* Optional
* Custom Repository Methods
* ResponseEntity
* HTTP Status Codes
* 400 Bad Request
* 201 Created
* 302 Found
* URL Redirection
* Request Validation
* Path Variables

---