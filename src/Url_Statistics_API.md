# URL Statistics API

## Problem

The application could:

* Create short URLs
* Redirect users
* Track click counts

However, there was no API to view this information.

Example:

```text
shortCode = ab12cd
clickCount = 15
```

The data existed in the database but could not be accessed through an endpoint.

---

## Goal

Create an endpoint that returns statistics for a shortened URL.

Endpoint:

```http
GET /stats/{shortCode}
```

Example:

```http
GET /stats/ab12cd
```

Response:

```json
{
  "originalUrl": "https://google.com",
  "shortUrl": "http://localhost:8080/ab12cd",
  "clickCount": 15
}
```

---

## New DTO

Created:

```java
public class UrlStatsResponseDTO {

    private String originalUrl;
    private String shortUrl;
    private Long clickCount;

}
```

Purpose:

```text
Transfer statistics data
from backend to client.
```

---

## Why Create a DTO?

Without DTO:

```text
Entity
   ↓
Directly exposed to client
```

Problems:

* Exposes internal database structure
* Difficult to customize responses
* Tight coupling between API and database

Using DTO:

```text
Entity
   ↓
DTO
   ↓
Client
```

Provides better separation of concerns.

---

## Service Layer Logic

Created method:

```java
public UrlStatsResponseDTO getUrlStats(
        String shortCode)
```

---

### Step 1

Find URL mapping.

```java
urlRepository.findByShortCode(shortCode)
```

---

### Step 2

If URL does not exist:

```java
.orElseThrow(
    () -> new ShortUrlNotFoundException(
            "Short code not found"
    )
)
```

Existing global exception handling automatically returns:

```http
404 Not Found
```

---

### Step 3

Populate DTO.

```java
response.setOriginalUrl(
        urlbody.getOriginalUrl());

response.setShortUrl(
        "http://localhost:8080/"
        + urlbody.getShortCode());

response.setClickCount(
        urlbody.getClickCount());
```

---

### Step 4

Return DTO.

```java
return response;
```

---

## Controller Endpoint

Created:

```java
@GetMapping("/stats/{shortCode}")
public ResponseEntity<UrlStatsResponseDTO>
getUrlStats(
        @PathVariable String shortCode)
```

---

### Flow

```text
Client
   ↓
GET /stats/ab12cd
   ↓
Controller
   ↓
Service
   ↓
Repository
   ↓
Database
   ↓
DTO
   ↓
JSON Response
```

---

## ResponseEntity.ok()

Used:

```java
return ResponseEntity.ok(responseDTO);
```

Equivalent to:

```java
return ResponseEntity
        .status(200)
        .body(responseDTO);
```

---

## HTTP Status Learned

### 200 OK

Meaning:

```text
Request completed successfully.
```

Example:

```http
GET /stats/ab12cd
```

Response:

```http
200 OK
```

---

## Example Response

Database:

```text
shortCode  = ab12cd
originalUrl = https://google.com
clickCount = 7
```

Request:

```http
GET /stats/ab12cd
```

Response:

```json
{
  "originalUrl":"https://google.com",
  "shortUrl":"http://localhost:8080/ab12cd",
  "clickCount":7
}
```

---

## Existing Exception Handling Reused

No new exception was needed.

Reused:

```java
ShortUrlNotFoundException
```

If short code does not exist:

```http
GET /stats/invalidCode
```

Response:

```http
404 Not Found
```

Body:

```text
Short code not found
```

---

## Concepts Learned

* DTO Design
* Data Retrieval APIs
* Analytics Endpoints
* ResponseEntity.ok()
* HTTP 200 OK
* Service Layer Data Mapping
* Reusing Existing Exceptions
* REST API Design

---

## API Endpoints So Far

### Create Short URL

```http
POST /shorten
```

---

### Redirect

```http
GET /{shortCode}
```

---

### Statistics

```http
GET /stats/{shortCode}
```

---

## Key Takeaway

Not all GET APIs return redirects.

Some GET APIs return data.

```text
GET /ab12cd
       ↓
302 Redirect

GET /stats/ab12cd
       ↓
200 OK + JSON Data
```

This commit introduced a read-only analytics endpoint that allows users to inspect information about shortened URLs.
