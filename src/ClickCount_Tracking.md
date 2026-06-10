# Click Count Tracking

## Problem

The URL shortener was redirecting users successfully, but there was no way to know how many times a short URL had been visited.

Example:

```text
GET /ab12cd
      ↓
Redirect to Google
```

After redirecting, no information was stored about the visit.

---

## Goal

Track the number of times a short URL is accessed.

Example:

```text
ab12cd
```

Visits:

```text
Visit 1 → clickCount = 1
Visit 2 → clickCount = 2
Visit 3 → clickCount = 3
```

---

## Entity Change

Added a new field:

```java
private Long clickCount = 0L;
```

Purpose:

```text
Store total number of visits
for each shortened URL.
```

---

## Database Schema Change

Before:

```text
id
originalUrl
shortCode
```

After:

```text
id
originalUrl
shortCode
clickCount
```

---

## Hibernate Observation

After adding the new field:

```java
private Long clickCount = 0L;
```

Hibernate created the column automatically because:

```properties
spring.jpa.hibernate.ddl-auto=update
```

However, existing database rows received:

```text
NULL
```

instead of:

```text
0
```

Reason:

```text
Hibernate updates the table structure,
but does not automatically populate
old rows with default values.
```

---

## Fixing Existing Data

Executed SQL:

```sql
UPDATE url_mapping_entity
SET click_count = 0
WHERE click_count IS NULL;
```

This converted old records from:

```text
NULL
```

to:

```text
0
```

---

## Incrementing Click Count

Inside:

```java
getOriginalUrlFromShortenUrl()
```

After finding the URL:

```java
url.setClickCount(
        url.getClickCount() + 1
);

urlRepository.save(url);
```

---

## Updated Redirect Flow

Before:

```text
GET /ab12cd
      ↓
Find URL
      ↓
Return Original URL
```

After:

```text
GET /ab12cd
      ↓
Find URL
      ↓
Increment clickCount
      ↓
Save Entity
      ↓
Return Original URL
```

---

## Example

Database Before:

```text
shortCode = ab12cd
clickCount = 5
```

User visits:

```text
GET /ab12cd
```

Database After:

```text
shortCode = ab12cd
clickCount = 6
```

---

## JPA Save Behavior

Used:

```java
urlRepository.save(url);
```

Important observation:

### New Entity

```java
save(newEntity)
```

Result:

```text
INSERT
```

Creates a new database row.

---

### Existing Entity

```java
save(existingEntity)
```

Result:

```text
UPDATE
```

Modifies an existing row.

---

## Concepts Learned

* Entity Modification
* Database Updates
* Click Tracking
* Analytics Basics
* Hibernate Schema Updates
* Existing vs New Entity
* save() for UPDATE operations
* Database Migration Considerations

---

## Key Takeaways

### Adding a Field to an Entity

```java
private Long clickCount = 0L;
```

creates the column for future records but does not update old database rows automatically.

---

### save() Can Perform Two Operations

```text
New Entity
    ↓
INSERT

Existing Entity
    ↓
UPDATE
```

---

### Read → Modify → Save Pattern

A common backend pattern:

```text
Read Entity
      ↓
Modify Data
      ↓
Save Entity
```

Used in:

* Click counters
* Inventory management
* Banking systems
* User statistics
* Analytics systems

---

## Final Flow

```text
GET /shortCode
        ↓
Find URL Mapping
        ↓
Increment Click Count
        ↓
Save Updated Entity
        ↓
Redirect User
```

### !!! The URL shortener now not only redirects users but also tracks usage statistics for every shortened URL.
