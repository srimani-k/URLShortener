# Redis Caching in URL Shortener

> Project: URL Shortener
>
> Tech Stack: Spring Boot + MySQL + Redis
>
> Module: Redis Integration & Cache-Aside Pattern

---

# Learning Objectives

After completing this module, you should understand:

- Why Redis is used
- Why caching is important
- Cache-Aside Pattern
- How Redis works with Spring Boot
- Why we created CachedUrl
- Serialization & Deserialization
- ObjectMapper
- Cache HIT vs Cache MISS
- Redis architecture
- Project architecture

---

# What problem are we trying to solve?

Before Redis, every redirect request looked like this:

```
Client
   │
   ▼
Spring Boot
   │
   ▼
 MySQL
```

Every single redirect hits MySQL.

Imagine this short URL becomes viral.

```
https://short.ly/abc123
```

Suppose:

- 10 million users open it today.

Without Redis:

```
10 Million Requests

↓

10 Million Database Queries
```

Problems:

- Database becomes overloaded.
- Higher latency.
- More CPU usage.
- More disk reads.
- Poor scalability.

Even though the original URL never changes, we're repeatedly asking MySQL for the same information.

That is wasteful.

---

# The Idea Behind Caching

Instead of asking MySQL every time,

store the result somewhere much faster.

That "somewhere" is called a **cache**.

Think of it like this.

You ask your friend:

> "What's John's phone number?"

First time:

He looks in his contacts.

Second time:

He already remembers it.

He answers immediately.

That's caching.

---

# What is Redis?

Redis stands for:

> **REmote DIctionary Server**

Redis is an **in-memory key-value database**.

Unlike MySQL,

Redis stores data inside RAM.

RAM is much faster than disks.

Approximate speeds:

| Storage | Speed |
|---------|--------|
| Hard Disk | milliseconds |
| SSD | hundreds of microseconds |
| Redis (RAM) | microseconds |

That's why Redis is extremely fast.

---

# Why use Redis?

Redis is commonly used for:

- Caching
- Sessions
- OTP storage
- Rate limiting
- Leaderboards
- Queues
- Counters
- Temporary data

For our project,

we're using Redis only as a **cache**.

---

# Project Architecture

Before Redis:

```
                Client
                   │
                   ▼
             Spring Boot
                   │
                   ▼
                 MySQL
```

After Redis:

```
                Client
                   │
                   ▼
             Spring Boot
             │          │
             ▼          ▼
          Redis      MySQL
```

Notice:

Spring Boot communicates with both.

Redis never talks directly to MySQL.

Spring Boot controls everything.

---

# Important Principle

Redis is **NOT** the source of truth.

MySQL is.

Redis is only an optimization.

If Redis disappears,

our application should still work.

That is exactly how we designed our project.

---

# What is Caching?

Caching means:

> Keeping frequently accessed data in a faster storage so future requests become quicker.

Instead of:

```
Client

↓

Database

↓

Response
```

We do:

```
Client

↓

Redis

↓

Found?

↓

YES → Response

NO

↓

Database

↓

Store in Redis

↓

Response
```

---

# Cache HIT vs Cache MISS

These are two extremely important interview terms.

## Cache HIT

The requested data already exists in Redis.

Example:

```
Request

↓

Redis

↓

Data Found

↓

Return immediately
```

No database query.

Very fast.

---

## Cache MISS

The requested data is not present in Redis.

Flow:

```
Redis

↓

Not Found

↓

MySQL

↓

Read Data

↓

Store in Redis

↓

Return Response
```

Only the first request is slow.

Future requests become fast.

---

# Cache-Aside Pattern ⭐

This is one of the most popular caching strategies used in backend systems.

Our URL Shortener implements the **Cache-Aside Pattern**.

## Definition

The application checks the cache first.

If the data is found (**Cache HIT**),

return it immediately.

If the data is not found (**Cache MISS**),

read it from the database,

store it in the cache,

and then return it.

The application itself is responsible for managing the cache.

---

# Why is it called "Cache-Aside"?

Because the cache sits **beside** the database.

```
           Redis
             ▲
             │
Application ─┼──── MySQL
```

The application decides when to use Redis.

Redis does not know anything about MySQL.

---

# Library Analogy 📚

Imagine:

MySQL is a huge library.

Redis is a small bookshelf beside your study table.

When you need a book:

1. Look at your bookshelf.
2. If it's there, read it.
3. If not, go to the library.
4. Bring the book back.
5. Keep a copy on your bookshelf.

Next time,

you don't need to walk to the library.

That is exactly how Cache-Aside works.

---

# Our Cache-Aside Flow

```
Receive Short Code

↓

Check Redis

↓

Cache HIT?
├─────────────┐
│             │
YES          NO
│             │
│             ▼
│        Read MySQL
│             │
│             ▼
│      Store in Redis
│             │
└─────────────┘
      │
      ▼
Return Original URL
```

This is exactly what our code does.

---

# What do we cache?

We intentionally do NOT cache the entire entity.

Instead,

we created:

```java
public class CachedUrl {

    private String originalUrl;

    private LocalDateTime expiresAt;

}
```

---

# Why not cache UrlMappingEntity?

Our entity contains many fields.

```
id

shortCode

originalUrl

clickCount

createdAt

expiresAt
```

For redirection,

we only need:

- originalUrl
- expiresAt

Everything else is unnecessary.

---

# Why create CachedUrl?

Advantages:

- Smaller object
- Less Redis memory
- Faster serialization
- Cleaner design
- Better separation of concerns

Interview Answer:

> Never cache unnecessary fields. Cache only the data required for the operation.

---

# Why don't we cache clickCount?

Suppose:

```
Redis

clickCount = 150
```

Application crashes.

Redis gets cleared.

Now,

clickCount is gone.

Business data should never depend on cache.

Therefore:

```
Original URL

↓

Redis
```

```
Click Count

↓

MySQL
```

MySQL remains the source of truth.

---

# Why don't we cache createdAt?

Because redirects never need it.

Keeping unnecessary fields increases cache size.

---

# Serialization

Redis does NOT understand Java objects.

Redis only understands bytes.

So before storing:

```
Java Object

↓

JSON

↓

Bytes

↓

Redis
```

This conversion is called:

> Serialization

---

# Deserialization

Reading is the reverse.

```
Redis

↓

Bytes

↓

JSON

↓

Java Object
```

This process is called:

> Deserialization

---

# Why JSON?

Instead of binary data,

JSON is:

- Human readable
- Language independent
- Easy to debug
- Widely supported

Example:

```json
{
  "originalUrl":"https://google.com",
  "expiresAt":"2027-06-27T17:54:40"
}
```

Much easier to inspect inside Redis.

---

# ObjectMapper

Jackson provides a class called:

```java
ObjectMapper
```

Its job is to convert between Java objects and JSON.

---

## Java Object → JSON

```java
String json =
objectMapper.writeValueAsString(cachedUrl);
```

This method serializes the object into a JSON string.

---

## JSON → Java Object

```java
CachedUrl cachedUrl =
objectMapper.readValue(
    json,
    CachedUrl.class
);
```

This method deserializes the JSON back into a Java object.

---

# Why do we pass CachedUrl.class?

Jackson needs to know **what type of object** to create.

Without it,

Jackson cannot reconstruct the Java object correctly.

Think of it like this:

```
JSON

↓

"What object should I create?"

↓

CachedUrl.class

↓

Create CachedUrl object
```

---

# Summary

Today we learned:

- Why Redis
- Why caching
- Cache HIT
- Cache MISS
- Cache-Aside Pattern
- Redis architecture
- Project architecture
- CachedUrl
- Why not cache everything
- Serialization
- Deserialization
- ObjectMapper
- JSON conversion

---

# Interview Questions

## 1. What is Redis?

**Answer:**

Redis is an in-memory key-value database commonly used for caching, sessions, rate limiting, leaderboards, and temporary data because it provides extremely fast read and write operations.

---

## 2. Why use Redis instead of MySQL?

**Answer:**

MySQL is designed for durable storage. Redis stores data in memory, making it much faster. Redis is used to reduce database load and improve response time, while MySQL remains the source of truth.

---

## 3. What is caching?

**Answer:**

Caching is the process of storing frequently accessed data in a fast storage layer so future requests can be served more quickly.

---

## 4. What is a Cache HIT?

**Answer:**

A Cache HIT occurs when the requested data is found in Redis, allowing the application to return it without querying the database.

---

## 5. What is a Cache MISS?

**Answer:**

A Cache MISS occurs when the requested data is not found in Redis. The application reads the data from MySQL, stores it in Redis, and returns it.

---

## 6. What is the Cache-Aside Pattern?

**Answer:**

Cache-Aside is a caching strategy where the application first checks the cache. On a cache miss, it reads from the database, stores the result in the cache, and returns it. The application is responsible for managing the cache.

---

## 7. Why did we create CachedUrl instead of caching the entity?

**Answer:**

We only cache the fields required for URL redirection (`originalUrl` and `expiresAt`). This reduces memory usage, improves performance, and keeps the cache focused on its purpose.

---

## 8. Why don't we cache clickCount?

**Answer:**

Click count is important business data. Since Redis is temporary, storing click counts only in Redis could lead to data loss. Therefore, MySQL remains the source of truth.

---

## 9. What is serialization?

**Answer:**

Serialization is the process of converting a Java object into JSON (or another format) so it can be stored or transmitted.

---

## 10. What is deserialization?

**Answer:**

Deserialization is the process of converting JSON back into a Java object.

---

## 11. What is ObjectMapper?

**Answer:**

`ObjectMapper` is a Jackson class that converts Java objects to JSON (`writeValueAsString`) and JSON back to Java objects (`readValue`).

---

## Key Takeaways

- Redis is an optimization, not the source of truth.
- Always keep important business data in the database.
- Cache only what is necessary.
- Cache-Aside is one of the most common caching strategies used in production systems.
- Serialization converts Java objects to JSON.
- Deserialization converts JSON back into Java objects.