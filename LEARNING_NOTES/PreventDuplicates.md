# Prevent Duplicate URLs

## Problem

Initially, every request generated a new short URL even if the original URL already existed.

Example:

```text
POST /shorten

https://google.com
        ↓
ab12cd
```

Sending the same URL again:

```text
POST /shorten

https://google.com
        ↓
xy89pq
```

Result:

```text
Same URL
      ↓
Multiple short URLs
```

This creates duplicate records and wastes storage.

---

## Goal

If a URL already exists in the database:

```text
https://google.com
```

Return the existing short URL instead of creating a new one.

---

## Repository Change

Added a custom repository method:

```java
Optional<UrlMappingEntity>
findByOriginalUrl(String originalUrl);
```

---

## How Spring Data JPA Works

Spring automatically generates a query based on the method name.

Method:

```java
findByOriginalUrl(String originalUrl)
```

Generated Query:

```sql
SELECT *
FROM url_mapping_entity
WHERE original_url = ?
```

No SQL needs to be written manually.

---

## Why Optional?

Return type:

```java
Optional<UrlMappingEntity>
```

Possible outcomes:

### URL Exists

```text
Optional
   ↓
UrlMappingEntity
```

### URL Does Not Exist

```text
Optional.empty()
```

This avoids returning null and reduces NullPointerException risk.

---

## Checking Existing URLs

Before generating a short code:

```java
Optional<UrlMappingEntity> existingUrl =
        urlRepository.findByOriginalUrl(inputurl);
```

Check if URL already exists:

```java
if(existingUrl.isPresent()) {

    UrlResponseDTO response =
            new UrlResponseDTO();

    response.setShortenUrl(
            "http://localhost:8080/"
            + existingUrl.get().getShortCode()
    );

    return response;
}
```

---

## Understanding isPresent()

```java
existingUrl.isPresent()
```

Meaning:

```text
Does Optional contain a value?
```

If true:

```java
existingUrl.get()
```

is safe to use.

---

## Updated Flow

```text
POST /shorten
        ↓
Receive URL
        ↓
Validate URL
        ↓
Check if URL exists
        ↓
YES
        ↓
Return existing short URL

NO
        ↓
Generate short code
        ↓
Save in database
        ↓
Return new short URL
```

---

## Example

### First Request

Input:

```json
{
  "url":"https://google.com"
}
```

Database:

```text
No matching URL
```

Action:

```text
Generate new short code
```

Response:

```json
{
  "shortenUrl":"http://localhost:8080/ab12cd"
}
```

---

### Second Request

Input:

```json
{
  "url":"https://google.com"
}
```

Database:

```text
URL already exists
```

Action:

```text
Return existing short URL
```

Response:

```json
{
  "shortenUrl":"http://localhost:8080/ab12cd"
}
```

No new database row is created.

---

## Benefits

### Prevents Duplicate Records

Instead of:

```text
google.com → ab12cd
google.com → xy89pq
google.com → mn45rt
```

Store only:

```text
google.com → ab12cd
```

---

### Saves Storage

Fewer rows in database.

---

### Consistent Results

Same URL always returns the same short URL.

---

## Concepts Learned

* Optional
* Optional.isPresent()
* Optional.get()
* Spring Data JPA Derived Query Methods
* findByOriginalUrl()
* Business Logic Validation
* Duplicate Prevention
* Database Lookup Before Insert

---

## Key Takeaway

Before creating a new URL mapping:

```text
Check Database
      ↓
Already Exists?
      ↓
YES → Return Existing Mapping
NO  → Create New Mapping
```

This is a common optimization used in real-world URL shortening systems.
