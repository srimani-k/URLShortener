
# RedisTemplate Deep Dive

So far, we have learned:

- Why Redis
- Why Cache
- Cache-Aside Pattern
- ObjectMapper

Now let's understand **how Spring Boot actually communicates with Redis**.

Spring Boot does **not** communicate with Redis directly.

Instead, it uses a helper class called:

```java
RedisTemplate
```

Think of `RedisTemplate` as Spring Boot's official Redis client.

Without it, we would have to manually send Redis commands over a TCP connection.

Instead of writing low-level networking code,

Spring provides:

```java
redisTemplate.opsForValue().set(...)
```

and

```java
redisTemplate.opsForValue().get(...)
```

making Redis extremely easy to use.

---

# What is RedisTemplate?

**Definition**

RedisTemplate is Spring Data Redis's abstraction for interacting with Redis.

It hides all the networking, serialization, and protocol details.

Instead of talking directly to Redis,

our application talks to RedisTemplate.

RedisTemplate then talks to Redis.

```
Application

↓

RedisTemplate

↓

Redis
```

---

# Why use RedisTemplate?

Imagine if RedisTemplate didn't exist.

To store one value, we would need to:

- Open a TCP socket
- Connect to Redis
- Send Redis protocol commands
- Serialize Java Objects
- Read the response
- Close the connection

That is a lot of work.

RedisTemplate does all of this automatically.

---

# Common Operations

Store data

```java
redisTemplate.opsForValue().set(key, value);
```

Read data

```java
redisTemplate.opsForValue().get(key);
```

Delete data

```java
redisTemplate.delete(key);
```

Check if key exists

```java
redisTemplate.hasKey(key);
```

Set expiry

```java
redisTemplate.expire(key, duration);
```

---

# Why opsForValue()?

Redis supports many data structures.

| Redis Data Structure | Spring Method |
|----------------------|---------------|
| String | opsForValue() |
| Hash | opsForHash() |
| List | opsForList() |
| Set | opsForSet() |
| Sorted Set | opsForZSet() |

Our URL Shortener stores:

```
shortCode

↓

JSON String
```

That's simply a key-value pair.

So we use:

```java
opsForValue()
```

---

# Why not use Hash?

We could have stored:

```
shortCode

↓

originalUrl

expiresAt
```

inside a Redis Hash.

But our object is very small.

JSON is simpler.

Less code.

Easier debugging.

---

# RedisConfig

Now let's understand every line.

Our final configuration:

```java
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, String> redisTemplate(
            RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, String> redisTemplate =
                new RedisTemplate<>();

        redisTemplate.setConnectionFactory(connectionFactory);

        redisTemplate.setKeySerializer(
                new StringRedisSerializer());

        redisTemplate.setValueSerializer(
                new StringRedisSerializer());

        return redisTemplate;
    }

}
```

Looks simple.

But every line is doing something important.

---

# @Configuration

```java
@Configuration
```

Definition:

Marks this class as a Spring configuration class.

Think of it as telling Spring:

> "This class contains object creation logic."

Spring scans it during application startup.

---

## Interview Definition

> `@Configuration` tells Spring that this class contains one or more `@Bean` methods that should be managed by the Spring container.

---

# Why not write this inside UrlService?

Imagine:

```java
new RedisTemplate<>();
```

inside every service.

Now suppose you have:

- UserService
- OrderService
- PaymentService
- UrlService

Each creates its own RedisTemplate.

Now you have multiple objects.

Bad design.

Instead,

Spring creates **one** RedisTemplate.

Everyone shares it.

---

# @Bean

Inside RedisConfig:

```java
@Bean
```

This tells Spring:

> "Execute this method once."

Spring stores the returned object.

Whenever another class needs it,

Spring injects the same object.

---

Think of it like this.

Without @Bean

```
Service A

↓

new RedisTemplate()
```

```
Service B

↓

new RedisTemplate()
```

```
Service C

↓

new RedisTemplate()
```

Three objects.

---

With @Bean

```
Spring

↓

Create ONE RedisTemplate

↓

Store it

↓

Share it everywhere
```

One object.

Reusable.

Efficient.

---

# Interview Question

### What does @Bean do?

**Answer**

`@Bean` tells Spring to create, manage, and reuse the returned object inside the Spring IoC container.

---

# What is Dependency Injection?

This is one of the most important Spring concepts.

Instead of writing:

```java
RedisTemplate redisTemplate =
        new RedisTemplate();
```

we simply ask Spring.

```java
private final RedisTemplate<String, String> redisTemplate;
```

Spring automatically provides it.

This is called:

> Dependency Injection (DI)

---

# Why is it called Dependency Injection?

Our UrlService depends on RedisTemplate.

Instead of creating the dependency,

Spring injects it.

```
UrlService

needs

↓

RedisTemplate

↓

Spring injects it
```

---

# Constructor Injection

Our service:

```java
public UrlService(

    UrlRepository repository,

    RedisTemplate<String, String> redisTemplate,

    ObjectMapper objectMapper
)
```

Notice something?

We never wrote:

```java
new RedisTemplate()
```

Spring did it.

---

# Why Constructor Injection?

Advantages:

✔ Mandatory dependencies

✔ Easier testing

✔ Immutable fields

✔ Recommended by Spring

✔ Better design

---

# Interview Question

Why Constructor Injection instead of Field Injection?

**Answer**

Constructor Injection makes dependencies explicit, allows immutable fields (`final`), improves testability, and avoids partially initialized objects.

---

# RedisConnectionFactory

This is another object created by Spring.

```java
RedisConnectionFactory
```

Think of it as:

> The object responsible for creating Redis connections.

Without it,

RedisTemplate has no idea where Redis is running.

---

Analogy:

Imagine sending a courier.

Before sending the package,

you need the destination address.

RedisConnectionFactory provides that address.

```
RedisTemplate

↓

Connection Factory

↓

Redis Server
```

---

# Package Analogy 📦

Think of Redis like mailing a package.

Step 1

Know where to send it.

↓

```
RedisConnectionFactory
```

Step 2

Pack it correctly.

↓

```
Serializer
```

Step 3

Send it.

↓

```
RedisTemplate
```

Exactly like:

```
Know destination

↓

Pack package

↓

Send package
```

This analogy is excellent for interviews because it helps explain the responsibilities of each component.

---

# What does setConnectionFactory() do?

```java
redisTemplate.setConnectionFactory(
    connectionFactory
);
```

This tells RedisTemplate:

> "Whenever you need to communicate with Redis, use this connection."

Without it,

RedisTemplate has no idea where Redis is located.

---

# Interview Question

What is RedisConnectionFactory?

**Answer**

RedisConnectionFactory creates and manages connections between the Spring application and the Redis server. RedisTemplate uses it whenever it needs to execute Redis commands.

---

# Why do we return RedisTemplate?

Notice:

```java
return redisTemplate;
```

That returned object becomes the Spring Bean.

Spring stores it inside its IoC Container.

Later,

when UrlService needs it,

Spring injects this exact object.

---

# IoC Container

Spring maintains a huge container.

Inside it,

all Beans are stored.

Example:

```
Spring Container

│

├── ObjectMapper

├── RedisTemplate

├── UrlRepository

├── UrlService

├── UrlController

├── RedisConnectionFactory

└── ...
```

Instead of creating objects,

Spring creates and manages them.

---

# Interview Questions

## 12. What is RedisTemplate?

**Answer**

RedisTemplate is Spring Data Redis's helper class that provides high-level APIs to interact with Redis without writing low-level networking code.

---

## 13. What does @Configuration do?

**Answer**

Marks a class as a configuration class that contains bean definitions managed by the Spring container.

---

## 14. What does @Bean do?

**Answer**

Creates and registers an object inside the Spring IoC container so it can be reused throughout the application.

---

## 15. What is Dependency Injection?

**Answer**

Dependency Injection is a design pattern where Spring provides required objects to a class instead of the class creating them manually.

---

## 16. Why use Constructor Injection?

**Answer**

Constructor Injection ensures required dependencies are available, supports immutable fields, improves testing, and is the recommended approach in Spring.

---

## 17. What is RedisConnectionFactory?

**Answer**

RedisConnectionFactory is responsible for creating and managing connections to the Redis server. RedisTemplate uses it to execute Redis operations.

---

# Part 2A Summary

In this section we learned:

- RedisTemplate
- `opsForValue()`
- Why RedisTemplate exists
- RedisConfig
- `@Configuration`
- `@Bean`
- Dependency Injection
- Constructor Injection
- RedisConnectionFactory
- Package analogy
- Spring IoC Container
- 6 interview questions

---

# Why Redis Stores Bytes

One important thing to understand is that **Redis does not understand Java objects**.

Redis is language-independent.

It doesn't know what:

```java
CachedUrl
```

or

```java
UrlMappingEntity
```

means.

Redis only understands **bytes**.

Think of Redis as a storage box.

It doesn't care what language created the data.

Whether the client is:

- Java
- Python
- Go
- Node.js
- C#

Redis stores only raw bytes.

```
Java Object

↓

Bytes

↓

Redis
```

So before storing data,

we must convert it into a format Redis understands.

---

# What is a Serializer?

A **Serializer** converts Java objects into bytes.

```
Java Object

↓

Serializer

↓

Bytes
```

When reading,

the reverse happens.

```
Bytes

↓

Deserializer

↓

Java Object
```

Every time we call:

```java
redisTemplate.opsForValue().set(...)
```

Spring asks:

> "How should I convert this Java object into bytes?"

That's exactly what a serializer decides.

---

# Our Initial Approach

Initially, we configured:

```java
RedisTemplate<String, CachedUrl>
```

We wanted RedisTemplate to automatically serialize and deserialize `CachedUrl`.

It looked clean.

But we encountered multiple problems.

---

# Problem 1 - LocalDateTime

Our `CachedUrl` contained:

```java
LocalDateTime expiresAt;
```

Jackson initially threw an exception because Java Time types require proper support.

Even though Spring Boot already configures `ObjectMapper`, we learned that serializers still need to use the correct mapper.

---

# Problem 2 - LinkedHashMap

Later, we saw:

```text
LinkedHashMap cannot be cast to CachedUrl
```

Why?

Because the generic serializer deserialized the JSON into a generic `Map` instead of our `CachedUrl` class.

Spring didn't know:

> "Should I create CachedUrl?"

or

> "Should I create a Map?"

Without explicit type information, it chose `LinkedHashMap`.

---

# Problem 3 - Weird Characters in Redis

When we opened RedisInsight,

instead of JSON we saw something like:

```text
���t�M...
```

At first it looked like corrupted data.

It wasn't.

Those bytes were produced by Java serialization.

Redis stored them correctly,

but humans couldn't read them.

Debugging became difficult.

---

# Our Final Decision

Instead of letting RedisTemplate serialize objects automatically,

we decided to control serialization ourselves.

Instead of:

```java
RedisTemplate<String, CachedUrl>
```

we changed to:

```java
RedisTemplate<String, String>
```

Now Redis stores **plain JSON strings**.

Spring no longer decides how objects are serialized.

Our application does.

---

# Why RedisTemplate<String, String>?

Now the flow becomes explicit.

```
CachedUrl Object

↓

ObjectMapper

↓

JSON String

↓

StringRedisSerializer

↓

Bytes

↓

Redis
```

When reading:

```
Redis

↓

Bytes

↓

String

↓

ObjectMapper

↓

CachedUrl
```

Everything is under our control.

This makes debugging much easier.

---

# Why is this Better?

Advantages:

✔ Human-readable JSON in Redis

✔ Easier debugging

✔ Explicit serialization

✔ Explicit deserialization

✔ No LinkedHashMap issues

✔ No hidden conversions

✔ Cleaner application logic

---

# Final RedisConfig

```java
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, String> redisTemplate(
            RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, String> redisTemplate =
                new RedisTemplate<>();

        redisTemplate.setConnectionFactory(connectionFactory);

        redisTemplate.setKeySerializer(
                new StringRedisSerializer());

        redisTemplate.setValueSerializer(
                new StringRedisSerializer());

        return redisTemplate;
    }

}
```

Notice something.

There is **no ObjectMapper here anymore.**

Why?

Because RedisTemplate is now storing Strings.

ObjectMapper is used only inside our service.

This separation makes responsibilities much clearer.

---

# StringRedisSerializer

Spring provides many serializers.

Examples:

- JdkSerializationRedisSerializer
- Jackson2JsonRedisSerializer
- GenericJackson2JsonRedisSerializer
- StringRedisSerializer

We chose:

```java
new StringRedisSerializer()
```

---

# What does StringRedisSerializer do?

It converts Java Strings into UTF-8 bytes.

Example:

```
JSON String

↓

UTF-8 Bytes

↓

Redis
```

When reading,

it converts those bytes back into the exact same String.

---

# Why is JSON Visible Now?

Remember when RedisInsight showed:

```text
���t�M...
```

That happened because Java Serialization stores binary data.

After switching to `StringRedisSerializer`,

Redis now stores:

```json
{
  "originalUrl":"https://google.com",
  "expiresAt":"2027-06-27T17:54:40"
}
```

Much easier to inspect.

Much easier to debug.

---

# Why Didn't It Work Immediately?

After changing the serializer,

you still saw the old binary values.

Why?

Because Redis already contained old cached entries.

Changing the serializer does **not** convert existing data.

The solution was:

1. Clear Redis.
2. Restart the application.
3. Generate new cache entries.

Then Redis stored JSON correctly.

---

# Time To Live (TTL)

Caches should not store data forever.

Otherwise,

memory keeps increasing.

Redis allows every key to have an expiry.

This is called:

> Time To Live (TTL)

---

# How We Calculate TTL

Our URLs already have a business expiry.

Example:

```
expiresAt

↓

2027-06-27
```

Instead of using a fixed TTL like:

```
30 Days
```

we calculate:

```java
Duration remainingTime =
        Duration.between(
            now,
            expiresAt
        );
```

This gives:

> Remaining lifetime of the URL.

---

# Storing with TTL

```java
redisTemplate.opsForValue().set(

        shortCode,

        cachedJson,

        remainingTime
);
```

Now Redis automatically removes the key after that duration.

No cleanup job required.

---

# Why Use Remaining Time?

Suppose:

URL expires after

```
12 Hours
```

If we cache it for

```
30 Days
```

Redis would keep an expired URL.

Instead,

our cache expires exactly when the business rule expires.

That keeps Redis and MySQL consistent.

---

# Why Do We Still Check expiresAt?

This is an excellent interview question.

Some people think:

> Redis TTL already removes expired data.

So why check:

```java
if(now.isAfter(cachedUrl.getExpiresAt()))
```

Answer:

Because **TTL is cache management.**

Business validation belongs to the application.

Think of TTL as:

> "Delete this cache entry."

Think of `expiresAt` as:

> "Is this URL still valid according to business rules?"

These are two different responsibilities.

Never rely on cache for business correctness.

---

# MySQL is the Source of Truth

One of the most important design decisions.

Redis is:

- Fast
- Temporary
- Replaceable

MySQL is:

- Persistent
- Reliable
- Source of Truth

If Redis disappears,

the application still works.

How?

```
Redis Empty

↓

Cache MISS

↓

Read MySQL

↓

Rebuild Cache
```

Nothing is lost.

---

# Why Click Count Stays in MySQL

Imagine:

```
Redis

clickCount = 1000
```

Server crashes.

Redis is cleared.

Now:

```
clickCount = 0
```

Business data has been lost.

Instead,

we update click counts directly in MySQL.

Redis only caches:

- originalUrl
- expiresAt

Important business data always remains safe.

---

# Design Principle

A cache should improve performance.

A cache should **never** become the only place where important data exists.

This is why we often say:

> Redis is an optimization, not the source of truth.

---

# Things to Remember

✅ Redis stores bytes, not Java objects.

✅ A serializer converts Java data into bytes.

✅ `StringRedisSerializer` stores readable UTF-8 strings.

✅ We manually convert objects to JSON using `ObjectMapper`.

✅ `RedisTemplate<String, String>` gives us full control over serialization.

✅ TTL should match the business expiry of the URL.

✅ Redis automatically removes expired cache entries.

✅ Business validation must still happen in the application.

✅ MySQL remains the source of truth.


---

# Common Mistakes We Encountered

One of the best ways to learn is by understanding the mistakes we made while building this project.

These bugs are common in real Spring Boot + Redis projects.

---

# Mistake 1 - Trying to Store Java Objects Directly

Initially we configured:

```java
RedisTemplate<String, CachedUrl>
```

It looked convenient because RedisTemplate could automatically serialize our object.

However, automatic serialization introduced complexity and hidden behavior.

We had less control over how objects were stored and read.

Eventually, we switched to manual JSON serialization using `ObjectMapper`.

### Lesson

> Explicit is often better than implicit.

When you control serialization yourself, debugging becomes much easier.

---

# Mistake 2 - LocalDateTime Serialization Error

We encountered:

```
Java 8 date/time type LocalDateTime not supported
```

### Why?

Jackson needs support for Java 8 Date/Time classes.

Fortunately, Spring Boot already configures an `ObjectMapper` with the required modules.

The problem happened because our serializer wasn't using Spring's configured `ObjectMapper`.

Once we started using Spring's injected `ObjectMapper`, the issue disappeared.

### Lesson

Always prefer Spring-managed beans over manually creating objects.

---

# Mistake 3 - LinkedHashMap cannot be cast to CachedUrl

We saw:

```
LinkedHashMap cannot be cast to CachedUrl
```

### Why?

The generic serializer knew it had JSON.

It did **not** know which Java class to recreate.

Without explicit type information,

it created:

```
LinkedHashMap
```

instead of:

```
CachedUrl
```

### How did we solve it?

Instead of relying on automatic deserialization,

we stored JSON strings.

Then we explicitly wrote:

```java
CachedUrl cachedUrl =
objectMapper.readValue(
        json,
        CachedUrl.class
);
```

Now Jackson knows exactly what object to create.

---

# Mistake 4 - Redis Showing Binary Data

RedisInsight showed:

```
���t...
```

instead of JSON.

### Why?

Java serialization stores binary bytes.

Redis was storing the data correctly,

but humans couldn't read it.

### Solution

Switch to:

```java
StringRedisSerializer
```

Now Redis stores:

```json
{
  "originalUrl":"https://google.com",
  "expiresAt":"2027-06-27T17:54:40"
}
```

Much easier to debug.

---

# Mistake 5 - Serializer Changed But Redis Still Showed Binary

After changing serializers,

Redis still displayed binary data.

### Why?

Because the old keys were already stored using the previous serializer.

Changing the serializer does **not** convert existing Redis data.

### Solution

- Flush Redis
- Restart Spring Boot
- Generate new cache entries

The new entries were stored as JSON.

---

# Mistake 6 - Trusting Redis Too Much

It is tempting to think:

```
Redis already has the data.

Done.
```

But remember:

Redis is only a cache.

Never trust cache for business correctness.

Always keep important business rules inside the application.

---

# Design Decisions We Took

Let's summarize the major decisions we made.

---

## Decision 1

Store only:

- originalUrl
- expiresAt

instead of the entire entity.

### Why?

Smaller cache.

Lower memory usage.

Faster serialization.

---

## Decision 2

Store clickCount only in MySQL.

### Why?

Redis is temporary.

Business data should never depend on cache.

---

## Decision 3

Use:

```java
RedisTemplate<String, String>
```

instead of:

```java
RedisTemplate<String, CachedUrl>
```

### Why?

We control serialization.

We control deserialization.

Debugging is easier.

---

## Decision 4

Use ObjectMapper manually.

### Why?

No hidden conversions.

Explicit code.

Predictable behavior.

---

## Decision 5

TTL equals business expiry.

Instead of:

```
30 Days
```

we calculate:

```
Remaining Lifetime
```

This keeps cache and business rules consistent.

---

# Engineering Mindset

Notice something about every decision?

We always asked:

> "What happens if Redis disappears?"

If the answer was:

```
Application still works.
```

then the design was good.

This is an important engineering principle.

Always design assuming your cache can fail.

---

# Interview Questions

---

## 18. Why did you switch to RedisTemplate<String, String>?

**Answer**

It gave us full control over serialization and deserialization. We manually converted objects to JSON using ObjectMapper, which avoided issues like LinkedHashMap deserialization and made debugging much easier.

---

## 19. Why use ObjectMapper manually?

**Answer**

Manual serialization makes the conversion process explicit. We know exactly when objects become JSON and when JSON becomes Java objects. This improves readability and debugging.

---

## 20. Why use StringRedisSerializer?

**Answer**

StringRedisSerializer stores UTF-8 strings. Since we already convert objects into JSON strings, this serializer stores readable JSON inside Redis instead of binary data.

---

## 21. Why not store business data only in Redis?

**Answer**

Redis is temporary. If Redis crashes or is cleared, the data would be lost. Therefore MySQL remains the source of truth.

---

## 22. What is TTL?

**Answer**

TTL (Time To Live) is the amount of time a Redis key should exist before Redis automatically deletes it.

---

## 23. Why calculate TTL using Duration.between()?

**Answer**

Because the remaining cache lifetime should exactly match the remaining business lifetime of the URL.

---

## 24. Why check expiresAt even after a Cache HIT?

**Answer**

TTL is a cache concern.

expiresAt is a business rule.

Business validation should never depend entirely on cache behavior.

---

## 25. Why not cache the entire entity?

**Answer**

Caching unnecessary fields wastes memory and increases serialization time. Cache only the data required for the operation.

---

## 26. Why did LinkedHashMap happen?

**Answer**

The serializer did not know which Java class to recreate during deserialization, so it produced a generic LinkedHashMap instead of CachedUrl.

---

## 27. Why did Redis show strange binary characters?

**Answer**

The previous serializer stored Java binary data. After switching to StringRedisSerializer and storing JSON strings, Redis displayed readable JSON.

---

## 28. What happens if Redis is unavailable?

**Answer**

The application should continue working by reading data from MySQL. Redis is an optimization layer, not a dependency for correctness.

---

## 29. Why do we catch Redis exceptions?

**Answer**

Because cache failures should not bring down the application. If Redis is unavailable, we simply fall back to MySQL.

---

## 30. What is the source of truth in your project?

**Answer**

MySQL.

Redis is only a cache used to improve performance.

---

# Mini Interview Scenario

**Interviewer:**

Why didn't you cache clickCount?

**You:**

Click count is important business data. Since Redis is temporary, storing click counts there could result in data loss if Redis crashes or is cleared. Therefore click counts are always updated in MySQL, while Redis only caches frequently read data such as originalUrl and expiresAt.

---

**Interviewer:**

Why did you manually serialize JSON instead of letting RedisTemplate do it?

**You:**

Manual serialization gave us full control over how data is stored and read. It also made debugging easier because Redis contains readable JSON, and we avoided generic deserialization issues such as LinkedHashMap.

---

**Interviewer:**

If Redis is down, what happens?

**You:**

The cache lookup fails, but the application reads from MySQL and continues serving requests. The cache is an optimization layer, not the source of truth.

---

# Best Practices

✅ Cache only what you need.

✅ Never trust cache as permanent storage.

✅ Keep important business data in the database.

✅ Handle Redis failures gracefully.

✅ Use readable JSON whenever possible.

✅ Match TTL with business expiry.

✅ Keep serialization explicit.

✅ Treat Redis as replaceable infrastructure.

---

# Revision Cheat Sheet

```
Client
   │
   ▼
Check Redis
   │
   ├── HIT
   │     │
   │     ▼
   │ Return URL
   │
   └── MISS
         │
         ▼
     Read MySQL
         │
         ▼
  Increment Click Count
         │
         ▼
 Serialize CachedUrl
         │
         ▼
 Store JSON in Redis
         │
         ▼
 Return URL
```

---

# Part 2 Complete 🎉

At this point you understand:

- Redis fundamentals
- RedisTemplate
- RedisConfig
- Dependency Injection
- ObjectMapper
- Serialization
- Deserialization
- StringRedisSerializer
- TTL
- Cache-Aside Pattern
- Engineering design decisions
- Common debugging issues
- Production best practices
- 30 interview questions (combined with Part 1 and Part 2)
