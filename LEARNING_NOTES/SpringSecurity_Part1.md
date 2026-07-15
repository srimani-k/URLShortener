# Spring Security - Part 1

# Security Foundation & Configuration

## Overview

Today, we started integrating **Spring Security** into the URL Shortener project.

Instead of directly implementing JWT authentication, we first understood how Spring Security works internally, why it exists, and how requests flow through the application before reaching our controllers.

The goal of today's session was to understand the **foundation** rather than simply copying configuration from tutorials.

---

# Why Spring Security?

Without Spring Security, every incoming request directly reaches the application.

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
Database
```

This means anyone can call our APIs.

Spring Security places a security layer before our application.

```
Client
   │
   ▼
Security Filter Chain
   │
   ▼
Controller
   │
   ▼
Service
   │
   ▼
Database
```

Every request is intercepted before entering our application.

---

# Authentication vs Authorization

These are two different concepts.

## Authentication

Authentication answers:

> **Who are you?**

Examples:

* Username & Password
* JWT Token
* OAuth

If authentication succeeds, the application knows the user's identity.

---

## Authorization

Authorization answers:

> **What are you allowed to access?**

Examples:

* Public APIs
* Protected APIs
* Admin-only APIs

Authentication always happens before Authorization.

---

# Why Authentication Should Not Be Done Inside Controllers

Suppose every controller contains:

```java
verifyJwt();

// business logic
```

Problems:

* Duplicate code
* Difficult maintenance
* Controllers become responsible for security
* Violates Separation of Concerns

Instead,

Authentication should happen before the request reaches controllers.

```
HTTP Request
        │
        ▼
Security Filter Chain
        │
        ▼
Controller
        │
        ▼
Service
```

Controllers should only contain business logic.

Authentication is infrastructure.

---

# Real World Analogy

Imagine visiting someone's house.

Bad approach:

```
Open door

↓

Invite stranger inside

↓

Ask who they are
```

Correct approach:

```
Security Guard

↓

Verify Identity

↓

Allow Entry
```

Spring Security acts as the security guard.

Controllers represent the house.

---

# SecurityConfig

Created:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
}
```

---

## Why @Configuration?

Marks the class as a configuration class.

Examples:

* RedisConfig
* SecurityConfig

Instead of business logic, these classes configure application behavior.

---

## Why @EnableWebSecurity?

Enables Spring Security for the application.

It tells Spring to use our custom security configuration instead of relying only on default security settings.

---

# SecurityFilterChain

Created a SecurityFilterChain Bean.

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http)
        throws Exception {

    return http.build();
}
```

---

# HttpSecurity

One of today's biggest concepts.

HttpSecurity is **not** the final security object.

It is a Builder.

Initially it contains default security configuration.

We customize it step by step.

Example:

```
Authentication ?

Authorization ?

CSRF ?

Session ?

Filters ?
```

Finally,

```java
http.build()
```

creates the finished SecurityFilterChain.

---

# Builder Pattern

Builder Pattern follows this idea:

```
Builder

↓

Configure

↓

Configure

↓

Configure

↓

build()

↓

Final Object
```

`HttpSecurity` follows exactly the same pattern.

We never return HttpSecurity itself.

We return the built object.

---

# Why build()?

Because HttpSecurity is still under construction.

Only after calling

```java
http.build()
```

does Spring create the actual SecurityFilterChain.

---

# Understanding @Bean

This was one of the biggest concepts discussed today.

Example:

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

Important understanding:

The method creates the object.

The @Bean annotation tells Spring:

> Execute this method during startup and manage the returned object as a Spring Bean.

Spring does NOT magically create the object.

The method creates it.

Spring stores and manages the returned object.

---

# Why Use @Bean?

Without @Bean:

```
new BCryptPasswordEncoder()

new BCryptPasswordEncoder()

new BCryptPasswordEncoder()
```

Objects are manually created everywhere.

With @Bean:

```
Application Startup

↓

Create Once

↓

Spring Manages It

↓

Inject Wherever Needed
```

Benefits:

* Dependency Injection
* Loose Coupling
* Single Managed Instance
* Cleaner Code

---

# Lambda Expressions

Spring Security uses Lambda expressions extensively.

Example:

```java
csrf -> csrf.disable()
```

The variable name is arbitrary.

These are identical:

```java
csrf -> csrf.disable()

config -> config.disable()

x -> x.disable()
```

Spring passes the configuration object.

We customize it using the lambda.

---

# Disabling CSRF

Configured:

```java
http.csrf(csrf -> csrf.disable());
```

Why?

Our project uses JWT.

JWT authentication is stateless.

CSRF mainly protects Session + Cookie based authentication.

Therefore it is commonly disabled for REST APIs using JWT.

---

# Authorization Rules

Configured:

```java
.authorizeHttpRequests(auth -> auth
        .requestMatchers("/register", "/login").permitAll()
        .anyRequest().authenticated()
)
```

---

## requestMatchers()

Matches specific request paths.

Examples:

```
/register

/login
```

It does not create endpoints.

It only matches incoming requests.

---

## permitAll()

Allows everyone to access these endpoints.

No authentication required.

Typical examples:

* Register
* Login
* Public APIs

---

## anyRequest()

Represents every remaining request.

Think of it as:

```
Everything Else
```

---

## authenticated()

Means:

The request must already be authenticated before entering the application.

---

# Rule Ordering

One of the most important lessons today.

Spring evaluates authorization rules from top to bottom.

Correct:

```java
.requestMatchers("/register", "/login").permitAll()
.anyRequest().authenticated()
```

Incorrect:

```java
.anyRequest().authenticated()
.requestMatchers("/register").permitAll()
```

Reason:

`anyRequest()` matches every remaining request.

If placed first,

Spring never evaluates the later rules.

---

# JWT Authentication Flow (Designed)

Today we designed the complete flow before implementation.

```
Request

↓

Read Authorization Header

↓

Header Present?

↓

Starts with "Bearer "?

↓

Extract JWT

↓

Validate JWT

↓

Authenticate User

↓

Continue Filter Chain

↓

Authorization Rules

↓

Controller
```

Implementation will be completed in upcoming sessions.

---

# Why JWT Filter Exists

Instead of writing:

```java
verifyJwt();
```

inside every controller,

Spring authenticates requests using a Filter.

Benefits:

* No duplicate authentication logic
* Cleaner controllers
* Better architecture
* Authentication happens before business logic

Authentication is a cross-cutting concern.

---

# Missing Authorization Header

One important concept learned today.

Suppose:

```
POST /login
```

contains no Authorization header.

Should the JWT Filter immediately return 401?

No.

The filter simply continues.

Later,

Authorization Rules decide:

```
/login

↓

permitAll()

↓

Allowed
```

If the filter rejected every request without a JWT,

Users would never be able to login.

This separation of responsibilities is important.

---

# JWT Validation

Validation means checking whether the JWT can be trusted.

Typical validations include:

* Signature Verification
* Expiration Check
* Correct JWT Format
* Valid User Information

Only after successful validation should the request be authenticated.

---

# Project Structure Decisions

Created / Planned packages:

```
config
controller
service
repository
entity
dto
security
cache
exception
```

---

## Why User belongs in entity

User represents a database table.

Therefore,

```
entity

↓

User
```

is the correct location.

---

## Why SecurityConfig belongs in config

SecurityConfig configures the application.

It behaves exactly like RedisConfig.

Therefore,

```
config

↓

SecurityConfig
```

---

## Why JWT Classes belong in security

Future classes:

```
JwtService

JwtAuthenticationFilter

CustomUserDetailsService
```

These are security infrastructure.

They are not business services.

Therefore they belong in:

```
security
```

---

## Why CachedUrlData belongs in cache

CachedUrlData is:

* Not a database Entity
* Not a Request/Response DTO

Its only purpose is storing Redis cache data.

Therefore:

```
cache

↓

CachedUrlData
```

is the cleanest design.

---

# Classes Created / Planned

Created:

* SecurityConfig
* User
* UserRepository
* RegisterRequestDTO
* LoginRequestDTO
* PasswordEncoder Bean

Planned:

* AuthController
* AuthService
* JwtService
* JwtAuthenticationFilter
* CustomUserDetailsService

---

# Design Decisions

* Security should be centralized.
* Authentication should happen before controllers.
* Business logic belongs in Services.
* JWT authentication should use Filters.
* Passwords should be stored as BCrypt hashes.
* Package structure should follow responsibilities.
* JWT classes belong in security.
* Cache models belong in cache.

---

# Production Thinking

Real production applications follow the same architecture:

* Requests enter Filters first.
* Authentication happens before Controllers.
* Controllers remain thin.
* Services contain business logic.
* Reusable infrastructure (Security, Redis, Logging) stays outside business code.

This separation improves maintainability and scalability.

---

# Common Mistakes

* Performing JWT validation inside Controllers.
* Storing plain text passwords.
* Placing `anyRequest()` before `requestMatchers()`.
* Mixing security logic with business logic.
* Creating objects manually instead of using Spring Beans.

---

# Interview Questions

### Why do we use Spring Security?

To authenticate and authorize requests before they reach application logic.

---

### Why should JWT validation happen in a Filter?

Because authentication is a cross-cutting concern that should be handled once before requests enter controllers.

---

### Why is `anyRequest()` placed last?

Because Spring evaluates authorization rules sequentially.

`anyRequest()` is a catch-all rule.

---

### Why use @Bean?

To let Spring manage reusable application objects created by our configuration methods.

---

### Why disable CSRF for JWT applications?

Because JWT-based REST APIs are stateless and do not rely on Session + Cookie authentication.

---

# Revision Summary

Today's key concepts:

* Spring Security architecture
* Authentication vs Authorization
* Security Filter Chain
* HttpSecurity Builder Pattern
* @Configuration
* @EnableWebSecurity
* @Bean
* Dependency Injection
* Lambda Expressions
* CSRF
* requestMatchers()
* permitAll()
* anyRequest()
* authenticated()
* Rule Ordering
* JWT Authentication Flow
* JWT Filter
* Project Package Structure
* Production Design Decisions

---

# Next Module

* Register API
* BCrypt Password Hashing
* Login API
* JWT Generation
* JWT Validation
* JWT Authentication Filter
* Protect URL Shortener APIs using JWT
