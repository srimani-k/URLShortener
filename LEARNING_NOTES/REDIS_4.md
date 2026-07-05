# PART 4 — Interview Revision Sheet, Cheat Sheet & Final Summary

> "Redis is not a database replacement.
> Redis is a performance optimization."

Congratulations!

You've now built a Redis integration yourself instead of just watching someone else do it.

That's a huge difference.

---

# 1. The Big Picture

Our URL Shortener architecture:

```
                Client
                   │
                   ▼
             Spring Boot API
                   │
        ┌──────────┴──────────┐
        ▼                     ▼
      Redis                MySQL
 (Cache Layer)      (Source of Truth)
```

Redis makes reads faster.

MySQL stores permanent business data.

---

# 2. Complete Request Flow

```
Client
   │
   ▼
Controller
   │
   ▼
Service
   │
   ▼
Check Redis
   │
 ┌─┴────────────┐
 │              │
 ▼              ▼
HIT            MISS
 │              │
 ▼              ▼
Read JSON    Read MySQL
 │              │
 ▼              ▼
Check Expiry Check Expiry
 │              │
 ▼              ▼
Update Click Update Click
 │              │
 ▼              ▼
Return URL  Store in Redis
                 │
                 ▼
            Return URL
```

---

# 3. Cache-Aside Pattern (One-Line Definition)

**Definition**

The application checks the cache first. If the data isn't present, it reads from the database, returns the result, and stores it in the cache for future requests.

---

# 4. Why Redis?

Without Redis

```
Request

↓

MySQL

↓

Response
```

Every request hits the database.

With Redis

```
Request

↓

Redis

↓

Response
```

Most requests never reach MySQL.

---

# 5. Why MySQL Is Still Needed

Redis is temporary.

MySQL is permanent.

Business data belongs in MySQL.

Cache exists only to improve performance.

---

# 6. Source of Truth

Our project:

```
Original URL

↓

MySQL
```

Click Count

↓

MySQL

Expiration

↓

MySQL

Redis stores copies.

Never originals.

---

# 7. What Did We Cache?

Only

```
CachedUrl

↓

originalUrl

expiresAt
```

Not

- clickCount

- id

- createdAt

- shortCode

because they are unnecessary for redirects.

---

# 8. Why JSON?

Advantages

✅ Human readable

✅ Easy debugging

✅ Language independent

✅ Portable

Example

```json
{
 "originalUrl":"https://google.com",
 "expiresAt":"2027-06-27T17:54:40"
}
```

---

# 9. Serialization Flow

```
Java Object

↓

ObjectMapper

↓

JSON

↓

Redis
```

Deserialization

```
Redis

↓

JSON

↓

ObjectMapper

↓

Java Object
```

---

# 10. TTL

TTL = Time To Live

Instead of storing

```
30 Days
```

we store

```
Remaining Lifetime
```

```
Duration.between(now, expiresAt)
```

This keeps Redis perfectly synchronized with business expiry.

---

# 11. Why Still Check expiresAt?

TTL is handled by Redis.

Expiration is handled by business logic.

Business rules should never depend entirely on infrastructure.

---

# 12. Why Use try-catch Around Redis?

Redis may fail because of

- network issues

- restart

- timeout

- serialization errors

Instead of crashing,

we fall back to MySQL.

---

# 13. Graceful Degradation

Bad

```
Redis down

↓

Application crashes
```

Good

```
Redis down

↓

MySQL

↓

Application still works
```

---

# 14. Why Use ObjectMapper Manually?

Advantages

✅ Explicit

✅ Predictable

✅ Easier debugging

✅ Easier testing

---

# 15. RedisConfig Summary

```
@Configuration

↓

Creates Beans

↓

@Bean

↓

RedisTemplate

↓

Injected Everywhere
```

Spring creates one shared RedisTemplate.

Every class uses the same instance.

---

# 16. Dependency Injection

Instead of

```java
new RedisTemplate()
```

Spring provides

```java
private final RedisTemplate<String,String>
```

Advantages

- loose coupling

- testability

- singleton management

---

# 17. Important Spring Classes

| Class | Purpose |
|---------|----------|
| RedisTemplate | Communicates with Redis |
| RedisConnectionFactory | Creates Redis connections |
| StringRedisSerializer | Converts String ↔ bytes |
| ObjectMapper | Converts Object ↔ JSON |
| CachedUrl | Cached DTO |
| Duration | Calculates TTL |

---

# 18. Common Redis Commands

Store

```
SET key value
```

Read

```
GET key
```

Remaining TTL

```
TTL key
```

Delete

```
DEL key
```

All keys

```
KEYS *
```

Flush everything

```
FLUSHALL
```

---

# 19. Redis Insight

RedisInsight is a GUI for Redis.

Useful for

- viewing keys

- checking TTL

- inspecting JSON

- debugging cache

---

# 20. Interview Questions (Quick Revision)

### Q1

What is Redis?

> An in-memory key-value data store used for caching, messaging, and fast data access.

---

### Q2

Why use Redis?

> To reduce database load and improve response time.

---

### Q3

Why is Redis fast?

> It stores data in RAM instead of reading from disk for every request.

---

### Q4

What is TTL?

> The time after which Redis automatically deletes a key.

---

### Q5

What is Cache-Aside?

> Check cache → if miss, read database → update cache → return response.

---

### Q6

Why MySQL as source of truth?

> Because Redis is temporary.

---

### Q7

Why store JSON?

> Easier debugging and portable across languages.

---

### Q8

Why ObjectMapper?

> Converts Java objects to JSON and back.

---

### Q9

Why Dependency Injection?

> Spring manages object creation and lifecycle.

---

### Q10

Why RedisTemplate?

> Provides high-level Redis operations.

---

### Q11

Why StringRedisSerializer?

> Stores readable UTF-8 strings instead of Java binary serialization.

---

### Q12

Why update click count in MySQL?

> Click count is business data that must persist.

---

### Q13

What happens if Redis crashes?

> Application falls back to MySQL.

---

### Q14

What happens during Cache HIT?

> Read Redis → Deserialize → Validate → Update click count → Return URL.

---

### Q15

What happens during Cache MISS?

> Read MySQL → Validate → Update click count → Cache JSON → Return URL.

---

# 21. Explain Redis in 2 Minutes (Interview Answer)

> Redis is an in-memory key-value data store commonly used as a cache to reduce database load and improve application performance. In our URL Shortener project, we implemented the Cache-Aside Pattern. Whenever a request arrives, the application first checks Redis. If the data is available, it returns it immediately after validating the expiration and updating the click count in MySQL. If Redis doesn't contain the data, the application fetches it from MySQL, stores a JSON copy in Redis with an appropriate TTL, and returns the response. MySQL remains the source of truth, while Redis serves only as a performance optimization. We also designed the application to gracefully fall back to MySQL if Redis is unavailable.

---

# 22. Explain *Your* Redis Implementation

> I integrated Redis into my Spring Boot URL Shortener using the Cache-Aside Pattern. I used `RedisTemplate<String, String>` and manually serialized a lightweight `CachedUrl` object into JSON using `ObjectMapper`. During redirects, the service first checks Redis. On a cache hit, it deserializes the JSON, validates expiration, updates the click count in MySQL, and returns the original URL. On a cache miss, it retrieves the record from MySQL, caches only the required fields with a TTL based on the remaining validity period, and then returns the URL. Redis failures are handled gracefully so the application continues working by falling back to MySQL.

---

# 23. Biggest Lessons Learned

While building this project, I learned that:

- Redis is an optimization layer, not the primary database.
- Serialization choices affect debugging and maintainability.
- TTL should reflect business rules.
- Infrastructure can fail, so applications should degrade gracefully.
- Small, focused cache objects improve efficiency.
- Good logging makes debugging significantly easier.

---

# 24. Final Mind Map

```
                   Redis
                     │
     ┌───────────────┼────────────────┐
     │               │                │
 Cache          Performance      In-Memory
     │
     ▼
Cache-Aside Pattern
     │
     ▼
RedisTemplate
     │
     ▼
ObjectMapper
     │
     ▼
JSON
     │
     ▼
TTL
     │
     ▼
Graceful Fallback
     │
     ▼
MySQL Source of Truth
```

---

# 25. Final Cheat Sheet

```
Redis = Cache

MySQL = Source of Truth

Redis stores JSON

ObjectMapper converts Object ↔ JSON

RedisTemplate talks to Redis

TTL = Automatic deletion

Cache-Aside Pattern

Cache HIT → Redis

Cache MISS → MySQL → Redis

Graceful degradation if Redis fails

Business data stays in MySQL

Cache only what you need
```

---

# Redis Module Complete 🎉

You can now confidently explain:

- What Redis is
- Why Redis is fast
- Cache-Aside Pattern
- TTL
- Serialization
- Deserialization
- RedisTemplate
- ObjectMapper
- Dependency Injection
- RedisConfig
- Graceful degradation
- Source of truth
- Production considerations
- Debugging decisions
- Your complete Redis integration in the URL Shortener project

More importantly, you didn't just learn Redis—you **built it**, debugged it, and understood the reasoning behind every design decision.

That experience is what interviewers value most.