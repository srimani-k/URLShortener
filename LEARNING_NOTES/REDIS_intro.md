# Redis Cache Introduction

## Goal

Until now, every redirect request looked up the original URL from MySQL.

```
Client
   │
   ▼
Spring Boot
   │
   ▼
MySQL
```

This works for small applications.

However, popular short URLs may be accessed thousands or even millions of times.

Reading from MySQL for every request becomes expensive.

To solve this, we introduce **Redis**, an in-memory database used as a cache.

---

# What is Redis?

Redis (Remote Dictionary Server) is an **in-memory key-value database**.

Unlike MySQL, Redis stores data primarily in RAM, making it extremely fast.

Example:

```
Key        Value
-------------------------------
88ef79  -> https://google.com
```

Redis is commonly used for:

* Caching
* Session storage
* Rate limiting
* Real-time counters
* Leaderboards
* Message queues

---

# Why Redis?

Imagine our application receives this request repeatedly.

```
GET /88ef79
```

Without Redis:

```
Client
   │
   ▼
Spring Boot
   │
   ▼
MySQL
```

Every request hits MySQL.

Even though the same data is requested again and again.

This wastes database resources.

---

# With Redis

```
             ┌──────────┐
             │  Redis   │
             └────┬─────┘
                  │
Client ──► Spring Boot
                  │
                  ▼
               MySQL
```

Spring Boot first checks Redis.

If the data exists, MySQL is not queried.

This greatly improves performance.

---

# Cache-Aside Pattern

We implemented the **Cache-Aside Pattern**.

Flow:

```
Request
   │
   ▼
Check Redis
   │
   ├─────────────┐
   │             │
Cache HIT     Cache MISS
   │             │
   ▼             ▼
Return URL    Read from MySQL
                  │
                  ▼
          Store in Redis
                  │
                  ▼
             Return URL
```

This is one of the most common caching strategies used in production systems.

---

# Cache HIT

A Cache HIT means Redis already contains the requested data.

Example:

Redis:

```
88ef79 -> https://google.com
```

Application:

```
GET 88ef79
```

Redis returns the URL immediately.

No database query is needed.

---

# Cache MISS

A Cache MISS means Redis does not contain the requested key.

Flow:

```
Redis
   │
   ▼
Not Found
   │
   ▼
MySQL
   │
   ▼
Return URL
```

After reading from MySQL, the application stores the data in Redis so future requests become Cache HITs.

---

# Installing Redis

Redis runs as a separate server.

It is not embedded inside Spring Boot.

Install using Homebrew:

```bash
brew install redis
```

Start Redis:

```bash
brew services start redis
```

Verify:

```bash
redis-cli ping
```

Expected output:

```
PONG
```

---

# Spring Boot Configuration

Added to `application.properties`:

```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

Explanation:

* `host` → Redis server location
* `port` → Redis default port (6379)

When Spring Boot starts, it automatically connects to Redis using these properties.

---

# Spring Boot Auto Configuration

Spring Boot automatically creates:

* RedisConnectionFactory
* RedisTemplate

This is similar to how Spring Boot automatically creates:

* DataSource
* EntityManager
* JpaRepository

No manual object creation is required.

---

# RedisTemplate

RedisTemplate is Spring Boot's helper class for communicating with Redis.

Comparison:

```
JpaRepository
      │
      ▼
     MySQL

RedisTemplate
      │
      ▼
     Redis
```

Instead of writing Redis commands manually, we use methods provided by RedisTemplate.

---

# Common Redis Operations

Store value:

```java
redisTemplate.opsForValue().set(key, value);
```

Equivalent Redis command:

```
SET key value
```

Retrieve value:

```java
redisTemplate.opsForValue().get(key);
```

Equivalent Redis command:

```
GET key
```

---

# Dependency Injection

We injected RedisTemplate exactly like UrlRepository.

```java
private final RedisTemplate<String, String> redisTemplate;
```

Constructor:

```java
public UrlService(
        UrlRepository urlRepository,
        RedisTemplate<String, String> redisTemplate
) {
    this.urlRepository = urlRepository;
    this.redisTemplate = redisTemplate;
}
```

Notice that we never created RedisTemplate ourselves.

Spring Boot created and injected it automatically.

---

# Redis in Our URL Shortener

Current implementation:

```
GET /88ef79

        │
        ▼
Redis
        │
        ├── HIT
        │      │
        │      ▼
        │ Return Original URL
        │
        └── MISS
               │
               ▼
            MySQL
               │
               ▼
        Return Original URL
```

---

# Logging

Added logs for:

```
Cache HIT
```

and

```
Cache MISS
```

This helps us understand whether requests are served from Redis or MySQL.

---

# Important Design Lesson

Initially we thought storing only the original URL in Redis would be enough.

Example:

```
Key
88ef79

Value
https://google.com
```

After implementing this, we discovered two important problems.

### Problem 1

Expired URLs could still be served from Redis.

Reason:

The application returned the cached URL without checking expiration.

---

### Problem 2

Click count stopped increasing.

Reason:

On a Cache HIT, the application returned immediately.

The code that increments click count never executed.

---

# Engineering Lesson

Caching is not simply about making applications faster.

When adding a cache, we must ask:

> **"What business logic am I skipping by returning cached data?"**

A cache should improve performance **without breaking correctness**.

This is one of the most important lessons in System Design.

---

# Next Step

The current cache stores only:

```
String
```

This is insufficient.

Next, we will redesign the cache to store a complete object:

```
CachedUrl

originalUrl
expiresAt
```

This allows the application to preserve business logic while still benefiting from Redis.

We will also learn:

* Object Serialization
* JSON Serialization
* Redis Configuration
* Custom Beans
* Spring @Configuration
* Spring @Bean
* RedisTemplate<String, CachedUrl>

These concepts will move our Redis implementation much closer to production-quality design.
