

#  UrlService Deep Dive

> "Caching is easy.
>
> Designing cache logic correctly is the difficult part."

Everything we've learned so far comes together inside one method:

```java
getOriginalUrlFromShortenUrl()
```

This method is a perfect example of the **Cache-Aside Pattern**.



# Our Goal

Whenever a client requests

```
GET /88ef79
```

we want to answer as fast as possible.

The fastest place is Redis.

If Redis doesn't have the data,

we use MySQL.

The client should never notice the difference.

---

# Complete Flow

```
              Client
                 │
                 ▼
        UrlController
                 │
                 ▼
           UrlService
                 │
        Check Redis Cache
                 │
        ┌────────┴────────┐
        │                 │
        ▼                 ▼
    Cache HIT         Cache MISS
        │                 │
        ▼                 ▼
 Deserialize JSON     Query MySQL
        │                 │
        ▼                 ▼
 Validate Expiry     Validate Expiry
        │                 │
        ▼                 ▼
Increment Click     Increment Click
        │                 │
        ▼                 ▼
 Return URL      Store in Redis
                       │
                       ▼
                  Return URL
```

Notice something.

No matter which path we take,

the client gets the same response.

Only the data source changes.

---

# Step 1

```java
LocalDateTime timeNow = LocalDateTime.now();
```

Why create this only once?

Imagine writing

```java
LocalDateTime.now()
```

five different times.

Each call returns a slightly different time.

Example:

```
10:30:01.000

10:30:01.150

10:30:01.220
```

Tiny difference.

Usually harmless.

But good backend code prefers consistency.

So we capture the current time once.

```
Current Time

↓

Reuse Everywhere
```

Cleaner.

Faster.

Consistent.

---

# Step 2

```java
String json =
redisTemplate.opsForValue().get(shortCode);
```

This is our cache lookup.

Possible outcomes:

```
Redis

↓

JSON
```

or

```
Redis

↓

null
```

Nothing else.

---

# Why String?

Remember our decision.

Instead of

```java
CachedUrl
```

Redis stores

```text
JSON String
```

Example:

```json
{
  "originalUrl":"https://google.com",
  "expiresAt":"2027-06-27T17:54:40"
}
```

Redis knows nothing about Java.

It simply stores text.

---

# Cache HIT

```
json != null
```

means

Redis already has the answer.

Excellent.

No database query is required for the URL itself.

---

# Why Deserialize Immediately?

```java
CachedUrl cachedUrl =
objectMapper.readValue(
        json,
        CachedUrl.class
);
```

JSON is just text.

Our application needs a Java object.

ObjectMapper performs this conversion.

```
JSON

↓

CachedUrl Object
```

Now we can call

```java
cachedUrl.getOriginalUrl()
```

instead of manually parsing JSON.

---

# Why Is This Inside try-catch?

Suppose Redis somehow contains:

```json
{
  bad json
```

ObjectMapper throws an exception.

Without a try-catch,

the entire request fails.

Instead,

we log the error

and continue with MySQL.

```
Redis Failure

↓

Catch Exception

↓

Fallback

↓

MySQL
```

This is graceful degradation.

---

# Graceful Degradation

One of the most important backend concepts.

Instead of crashing,

the application becomes slightly slower.

Users still get the correct response.

Always prefer:

```
Slow

instead of

Broken.
```

---

# Business Validation

Next,

we check

```java
expiresAt
```

```
if(now.isAfter(...))
```

Notice something.

We validate **after** deserialization.

Why?

Because

before deserialization,

we only have text.

Business rules belong to objects,

not raw JSON strings.

---

# Cache Does NOT Decide Business Rules

Redis answers:

```
Do I have data?
```

Our application answers:

```
Should this URL still work?
```

Never mix these responsibilities.

---

# Increment Click Count

Even after a Cache HIT,

we still update MySQL.

```
Cache HIT

↓

Database Update
```

Why?

Because

click count is business data.

Redis is only a cache.

MySQL is the source of truth.

---

# Why Not Update Redis Instead?

Imagine this.

```
Click Count

↓

Stored only in Redis
```

Server crashes.

Redis restarts.

Everything disappears.

Your analytics are gone.

That's unacceptable.

Business data belongs in persistent storage.

---

# Cache MISS

Suppose

```
Redis

↓

null
```

Now we move to MySQL.

```
Redis

↓

MISS

↓

MySQL
```

Notice something.

This is exactly what Cache-Aside says.

The cache is checked first.

Database is second.

---

# Reading MySQL

```java
findByShortCode()
```

returns

```
Optional<UrlMappingEntity>
```

Why Optional?

Because

the short code may not exist.

Optional forces us to think about missing data.

Instead of

```
NullPointerException
```

we explicitly handle

```
ShortUrlNotFoundException
```

Much cleaner.

---

# Business Validation Again

After reading MySQL,

we again check

```
expiresAt
```

Same business rule.

Different source.

Whether data came from Redis

or

MySQL,

the rule is identical.

Consistency matters.

---

# Increment Click Count

Again,

we update MySQL.

Why?

Every successful redirect counts as one visit.

Business data must stay accurate.

---

# Storing Back Into Redis

This is the most important Cache-Aside step.

```
Database

↓

Application

↓

Redis
```

Without this step,

every request would remain a Cache MISS.

The cache would never warm up.

---

# Creating CachedUrl

Instead of storing the whole entity,

we create

```java
CachedUrl
```

containing only:

- originalUrl
- expiresAt

Why?

Because

the cache should contain only what is needed.

Smaller objects

↓

Less memory

↓

Faster serialization

↓

Faster network transfer

---

# Object → JSON

```java
objectMapper.writeValueAsString(...)
```

converts

```
CachedUrl

↓

JSON
```

Again,

Redis stores only text.

---

# Calculate Remaining TTL

```java
Duration.between(
    now,
    expiresAt
)
```

This produces

```
Remaining Lifetime
```

instead of

```
Fixed Lifetime
```

That means

Redis automatically removes the key

exactly when the URL expires.

Beautiful synchronization.

---

# Store in Redis

Finally,

```
Redis

↓

JSON

↓

TTL
```

Now the next request becomes

```
Cache HIT
```

instead of

```
Cache MISS
```

Exactly what we wanted.

---

# Package Delivery Analogy

Imagine ordering something online.

Redis is like

your apartment security desk.

```
Need package?

↓

Check Security Desk
```

If they already have it,

you get it immediately.

If not,

they contact the warehouse.

```
Warehouse

↓

Package arrives

↓

Security Desk stores it

↓

Next visitor gets it instantly
```

That's exactly how Cache-Aside works.

---

# Backend Engineer Mindset

While writing this method,

we constantly asked ourselves:

- What if Redis is empty?
- What if Redis crashes?
- What if JSON is invalid?
- What if the URL has expired?
- What if the short code doesn't exist?
- What if serialization fails?

Good backend developers don't just write the happy path.

They design for failures first.

That's what makes software reliable.

---

# Key Takeaways

✅ Always check the cache first.

✅ Never trust the cache for business correctness.

✅ Deserialize safely.

✅ Catch Redis-related exceptions and fall back gracefully.

✅ Update business data in MySQL.

✅ Populate Redis after a cache miss.

✅ Cache only the fields you actually need.

✅ TTL should match the business expiry.

✅ Design assuming Redis can fail at any time.

---

## Part 3A Complete 🎉

You now understand not just **what** your `UrlService` does, but **why every line exists**.

More importantly, you've started thinking like a backend engineer: designing for correctness first, then performance.


---

# PART 3B — Debugging, Best Practices & Production Thinking

> "Software engineering isn't about writing perfect code.
> It's about writing code that behaves correctly even when things go wrong."

By now, our URL Shortener works.

But along the way, we hit several bugs.

Each bug taught an important engineering lesson.

---

# Debugging Journey

Let's revisit every major issue we solved.

These weren't random errors—they taught us how Spring Boot, Redis, and Jackson actually work.

---

# Bug #1 — LocalDateTime Serialization Error

### Error

```
Java 8 date/time type LocalDateTime not supported
```

### Why did it happen?

Our `CachedUrl` contained:

```java
LocalDateTime expiresAt;
```

Jackson can serialize most Java objects.

However, Java 8 Date/Time classes require additional support.

Although Spring Boot already provides this support through its configured `ObjectMapper`, our serializer wasn't using that configuration correctly.

### Solution

Instead of creating our own mapper or relying on Redis serialization,

we injected Spring's `ObjectMapper`.

```java
private final ObjectMapper objectMapper;
```

Now both serialization and deserialization work correctly.

---

# Lesson

Always use Spring-managed beans whenever possible.

Spring has already configured them correctly.

---

# Bug #2 — LinkedHashMap cannot be cast to CachedUrl

### Error

```
LinkedHashMap cannot be cast to CachedUrl
```

### Why?

Redis successfully stored JSON.

During deserialization,

the serializer didn't know which Java class to recreate.

Without explicit type information,

Jackson produced

```
LinkedHashMap
```

instead of

```
CachedUrl
```

### Solution

Instead of automatic conversion,

we manually wrote

```java
String json =
redisTemplate.opsForValue().get(shortCode);

CachedUrl cachedUrl =
objectMapper.readValue(
        json,
        CachedUrl.class
);
```

Now Jackson knows exactly which object to build.

---

# Lesson

Automatic serialization is convenient.

Explicit serialization is predictable.

Predictability is often better.

---

# Bug #3 — Binary Data in Redis

Redis contained

```
��...
```

instead of JSON.

### Why?

Java serialization stores binary bytes.

Redis stored the data correctly,

but humans couldn't read it.

### Solution

Use

```java
StringRedisSerializer
```

for both

- keys
- values

Now Redis stores

```json
{
  "originalUrl":"https://google.com",
  "expiresAt":"2027-06-27T17:54:40"
}
```

Much easier to debug.

---

# Lesson

Store readable data whenever possible.

Future you will thank present you.

---

# Bug #4 — Serializer Changed But Redis Didn't

After changing serializers,

Redis still displayed binary values.

### Why?

Existing keys were already stored using the old serializer.

Changing configuration only affects **new writes**.

It doesn't convert existing data.

### Solution

```
Flush Redis

↓

Restart Spring Boot

↓

Generate new cache entries
```

Everything was stored as JSON afterwards.

---

# Lesson

Configuration changes rarely modify existing data.

Always consider what's already stored.

---

# Bug #5 — Cache Exceptions

Suppose Redis is unavailable.

Or JSON becomes corrupted.

Should the application crash?

No.

We wrapped cache operations inside

```java
try {
    ...
}
catch (...)
```

If Redis fails,

we simply read from MySQL.

---

# Lesson

Redis is an optimization.

Not a dependency.

---

# Logging Strategy

Good logs help you understand what happened without opening the debugger.

Here's the strategy we followed.

---

## INFO

Used for normal application flow.

Examples

```java
Cache HIT

Cache MISS

Stored in Redis

Redirecting

Click count updated
```

These logs tell the story of a successful request.

---

## WARN

Used when something unexpected happens,

but the application continues.

Examples

```
Redis unavailable

Expired URL

Cache deserialization failed

Short code not found
```

The application still runs,

but attention may be needed.

---

## ERROR

Used when the request cannot continue.

Examples

```
Database unavailable

Unexpected runtime exception

Application startup failure
```

Errors usually require immediate investigation.

---

# Why We Didn't Log Everything

Logging every line sounds helpful.

It isn't.

Too many logs create noise.

The important events become harder to find.

Good logging answers three questions.

1. What happened?

2. Why did it happen?

3. What happened next?

---

# Production Improvements

Our application is good.

But how would companies improve it?

Let's think like backend engineers.

---

# Improvement 1 — Click Count

Current flow

```
Request

↓

Update Database

↓

Return Response
```

Every request performs a database update.

At

```
10 million requests/day
```

that's expensive.

---

## Better Approach

```
Request

↓

Redis Counter

↓

Background Worker

↓

Database
```

The user gets an instant response.

The database is updated periodically.

Much faster.

---

# Improvement 2 — Asynchronous Processing

Instead of

```
Request

↓

Database Update
```

we can do

```
Request

↓

Queue

↓

Worker

↓

Database
```

Popular tools

- Kafka
- RabbitMQ
- AWS SQS

The request becomes much faster.

---

# Improvement 3 — Cache Stampede

Imagine

1000 users request

```
abc123
```

at the exact same moment.

Cache is empty.

All 1000 requests hit MySQL.

This is called

```
Cache Stampede
```

Solutions include:

- Distributed locks
- Request coalescing
- Background cache warming

---

# Improvement 4 — Cache Penetration

Suppose users continuously request

```
abcdef
```

which doesn't exist.

Every request reaches MySQL.

Possible solutions

- Cache negative results briefly
- Bloom Filters

---

# Improvement 5 — Redis Cluster

One Redis server eventually becomes insufficient.

Production systems often use

```
Redis Cluster
```

Benefits

- Horizontal scaling
- Automatic partitioning
- High availability

---

# Improvement 6 — Monitoring

Applications need visibility.

Useful metrics include

- Cache hit ratio
- Cache miss ratio
- Redis latency
- Database latency
- Requests per second
- Error rate

Popular monitoring tools

- Prometheus
- Grafana

---

# Engineering Principles We Followed

Let's summarize the design philosophy.

---

## Principle 1

MySQL is the source of truth.

Redis is temporary.

---

## Principle 2

Cache should improve performance,

not correctness.

---

## Principle 3

Business validation belongs in the application.

---

## Principle 4

Cache only what is frequently read.

---

## Principle 5

Always design for failure.

Servers fail.

Networks fail.

Caches fail.

Your application should continue working.

---

# System Design Connection

Our project already demonstrates several real-world concepts.

✅ Cache-Aside Pattern

✅ Read Optimization

✅ TTL Management

✅ Graceful Degradation

✅ Separation of Concerns

✅ Source of Truth

✅ Serialization

✅ Deserialization

✅ Dependency Injection

These are common topics in backend and system design interviews.

---

# Interview Questions

---

## 31. What happens during a Cache HIT?

Redis returns the cached JSON, the application deserializes it into a `CachedUrl`, validates expiration, updates the click count in MySQL, and returns the original URL.

---

## 32. What happens during a Cache MISS?

The application reads from MySQL, validates expiration, increments the click count, serializes the required data into JSON, stores it in Redis with a TTL, and returns the original URL.

---

## 33. Why use try-catch around Redis operations?

Redis failures should not stop the application. If cache access fails, we gracefully fall back to MySQL.

---

## 34. Why is MySQL the source of truth?

Because Redis is temporary. Important business data must survive cache failures and restarts.

---

## 35. What is graceful degradation?

Instead of failing completely when a dependency is unavailable, the application continues operating with reduced performance.

---

## 36. Why not cache click counts?

Click counts are business data. Storing them only in Redis risks losing data if Redis is cleared or crashes.

---

## 37. Why store JSON instead of Java objects?

JSON is language-independent, human-readable, easier to debug, and avoids serializer-specific issues.

---

## 38. Why do we use TTL?

TTL ensures cached data disappears automatically when it is no longer valid, keeping Redis aligned with business rules.

---

## 39. How would you scale this application?

Possible improvements include:
- Redis Cluster
- Asynchronous click counting
- Message queues
- Load balancing
- Database replication
- Monitoring and alerting

---

## 40. What's the biggest lesson from this project?

Caching is not just about making applications faster. It's about improving performance **without sacrificing correctness or reliability**.

---

# Final Thoughts

When we started, Redis probably seemed like:

> "Just another database."

Now you know it's much more than that.

You learned:

- how Redis fits into an application
- why serialization matters
- how TTL works
- why MySQL remains the source of truth
- how to debug serialization issues
- how to design for failures
- how backend engineers think about production systems

Those are skills that transfer far beyond this URL Shortener.

---

## End of Part 3 🎉

You now understand not only **how** your Redis integration works, but **why it was designed this way** and **how it could evolve into a production-scale system**.