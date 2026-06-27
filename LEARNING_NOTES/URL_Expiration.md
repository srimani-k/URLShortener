# URL Expiration

## Objective

Add an expiration mechanism so that short URLs become invalid after a certain period of time.

In our project, every generated short URL expires **30 days** after it is created.

---

# Why Do We Need URL Expiration?

Many real-world applications generate temporary links instead of permanent ones.

Examples:

* Password Reset Links
* Email Verification Links
* OTP Verification
* Temporary Download Links
* Invitation Links
* Trial Access Links

These resources should not remain valid forever.

Similarly, our URL Shortener should also support temporary URLs.

---

# Business Requirement

When a user creates a short URL:

```text
Created Time
↓

Current Time
```

The application automatically calculates:

```text
Expiration Time = Created Time + 30 Days
```

If the user visits the URL after the expiration time:

* Do not redirect.
* Return an error indicating that the URL has expired.

---

# Database Changes

Added two new fields to the entity.

```java
private LocalDateTime createdAt;
private LocalDateTime expiresAt;
```

---

# Why LocalDateTime Instead of String?

Bad Approach:

```java
private String expiresAt;
```

Problems:

* Difficult to compare dates
* Requires parsing every time
* More error-prone

Better Approach:

```java
private LocalDateTime expiresAt;
```

Advantages:

* Easy comparison
* Easy date calculations
* Built into Java Time API
* Recommended for Spring Boot applications

---

# Why Store Both createdAt and expiresAt?

Initially, I thought storing only:

```java
private LocalDateTime createdAt;
```

would be enough.

Since:

```text
Expiration = Created Time + 30 Days
```

could be calculated whenever needed.

This approach works only when **every URL always follows the same expiration rule.**

---

## Why Store expiresAt?

Real applications often have different expiration policies.

Example:

```text
Free User
30 Days

Premium User
180 Days

Enterprise User
Never Expires
```

Now every URL has its own expiration.

Instead of recalculating using business rules every request, we simply store:

```java
expiresAt
```

and compare against the current time.

This makes the system flexible for future requirements.

---

# Calculation vs Storage

This feature introduced an important design decision.

## Option 1 - Calculate

Store only:

```java
createdAt
```

Every request:

```text
expiresAt = createdAt + 30 days
```

Pros:

* Less storage

Cons:

* Assumes every record follows the same rule
* Harder to support different expiration policies

---

## Option 2 - Store

Store:

```java
createdAt
expiresAt
```

Pros:

* Each record remembers its own expiration
* Easy to support Premium plans
* Easy to support custom durations
* Faster expiration checks

Cons:

* One additional database column

Production systems usually prefer this approach.

---

# Setting the Dates

While creating the short URL:

```java
LocalDateTime now = LocalDateTime.now();

url.setCreatedAt(now);
url.setExpiresAt(now.plusDays(30));
```

---

# Why Store LocalDateTime.now() in a Variable?

Instead of:

```java
url.setCreatedAt(LocalDateTime.now());
url.setExpiresAt(LocalDateTime.now().plusDays(30));
```

we use:

```java
LocalDateTime now = LocalDateTime.now();
```

Reason:

Both fields now use exactly the same timestamp.

This is cleaner and avoids tiny differences caused by calling `now()` multiple times.

---

# Where Should Expiration Be Checked?

Three possible layers:

Controller

Service

Repository

Correct Answer:

**Service Layer**

Reason:

Expiration is a business rule.

Controllers should only receive requests and return responses.

Repositories should only communicate with the database.

Business logic belongs inside the Service layer.

---

# Redirect Flow

```text
Client

↓

Controller

↓

Service

↓

Find URL

↓

Check Expiration

↓

Increment Click Count

↓

Save

↓

Return Original URL

↓

302 Redirect
```

---

# Expiration Check

```java
if(LocalDateTime.now().isAfter(url.getExpiresAt())){
    throw new UrlExpiredException("URL has expired");
}
```

Meaning:

Current Time

>

Expiration Time

↓

URL is no longer valid.

---

# Why Throw an Exception?

Instead of:

```java
return null;
```

we throw:

```java
throw new UrlExpiredException(...);
```

Advantages:

* Cleaner Controller
* No null checks
* Uses Global Exception Handler
* Consistent error responses

---

# Order of Operations

Correct order:

```text
Find URL

↓

Check Expiration

↓

Increment Click Count

↓

Save

↓

Return Original URL
```

Important:

Click count should **not** increase for expired URLs.

Checking expiration first keeps analytics accurate.

---

# HTTP Status Code

Initially I used:

```http
401 Unauthorized
```

This was incorrect.

Reason:

401 means:

"The user is not authenticated."

An expired URL is not an authentication problem.

The correct status code is:

```http
410 Gone
```

Meaning:

"The resource existed previously but is no longer available."

This perfectly describes an expired short URL.

---

# Concepts Learned

* LocalDateTime
* Date Calculations
* plusDays()
* isAfter()
* Temporary Resources
* Business Rules
* Calculation vs Storage
* Service Layer Responsibilities
* URL Expiration
* HTTP 410 Gone
* Exception-Based Error Handling
* Order of Business Logic

---

# Key Takeaways

* Temporary resources should have expiration.
* LocalDateTime is preferred over String for dates.
* Store important business decisions when future flexibility is needed.
* Business logic belongs in the Service layer.
* Expired URLs should not increase click count.
* Throw exceptions instead of returning null.
* Use HTTP 410 Gone for expired resources.
