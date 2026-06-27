# Collision-Safe Short Code Generation

## Objective

Ensure that every generated short code is unique before saving it to the database.

This prevents two different URLs from receiving the same short code.

---

# Problem Statement

Initially, the application generated short codes like this:

```java
String generateShortCode = UUID.randomUUID().toString().substring(0,6);
```

Example:

```
https://google.com
        ↓
      ab12cd
```

This works most of the time.

However, UUID generation is random.

Random **does not mean impossible to repeat**.

A collision can happen.

Example:

```
google.com
      ↓
ab12cd

facebook.com
      ↓
ab12cd
```

Now two different URLs have the same short code.

When someone visits:

```
GET /ab12cd
```

The application does not know whether to redirect to Google or Facebook.

This breaks the entire URL Shortener.

---

# Understanding Two Different Types of Duplicates

During implementation, I initially thought duplicate URL prevention and duplicate short code prevention were the same.

They are completely different.

---

## 1. Duplicate Original URL Prevention ✅

Already implemented earlier.

Repository:

```java
findByOriginalUrl(inputUrl)
```

Purpose:

```
Has this URL already been shortened?
```

Example:

```
google.com
      ↓
Already exists?
      ↓
YES
      ↓
Return existing short URL
```

No new short code is generated.

This prevents:

```
google.com
↓

ab12cd

google.com
↓

xy789a
```

for the same URL.

---

## 2. Duplicate Short Code Prevention ✅

New feature.

Repository:

```java
findByShortCode(shortCode)
```

Purpose:

```
Has this generated short code already been assigned to another URL?
```

Example:

```
Generate

↓

ab12cd

↓

Already exists?

↓

YES

↓

Generate another one
```

This prevents:

```
google.com
↓

ab12cd

facebook.com
↓

ab12cd
```

---

# Goal

Generate a random short code.

Before saving:

* Check if it already exists.
* If it exists, generate another one.
* Repeat until a unique short code is found.

---

# Algorithm

```
Generate Short Code

↓

Already Exists?

↓

YES

↓

Generate Again

↓

Already Exists?

↓

YES

↓

Generate Again

↓

NO

↓

Save
```

---

# Why Use a while Loop?

Question:

How many attempts are required before finding a unique code?

Answer:

Unknown.

Possible scenarios:

```
Attempt 1

↓

Unique
```

or

```
Attempt 1

↓

Collision

↓

Attempt 2

↓

Unique
```

or

```
Attempt 1

↓

Collision

↓

Attempt 2

↓

Collision

↓

Attempt 3

↓

Unique
```

Since the number of iterations is unknown beforehand, a **while loop** is the correct choice.

---

# Why Not a for Loop?

A for loop is used when we know exactly how many iterations are needed.

Example:

```java
for(int i=0;i<10;i++)
```

Here we do not know whether we need:

* 1 iteration
* 2 iterations
* 10 iterations

Therefore a for loop is not suitable.

---

# Implementation

```java
String generateShortCode = generateShortCodeWithUUID();

while(urlRepository.findByShortCode(generateShortCode).isPresent()){
    generateShortCode = generateShortCodeWithUUID();
}
```

Explanation:

Generate a random short code.

Check database.

If it already exists:

Generate another one.

Repeat until it becomes unique.

---

# Helper Method

Initially the code was written twice:

```java
UUID.randomUUID().toString().substring(0,6);
```

Repeated code makes maintenance difficult.

Created helper method:

```java
private String generateShortCodeWithUUID(){
    return UUID.randomUUID().toString().substring(0,6);
}
```

Benefits:

* Cleaner code
* Reusable
* Easier maintenance
* Follows DRY (Don't Repeat Yourself)

---

# Mistake I Made

Initially I wrote:

```java
private String generateShortCodeWithUUID(String url)
```

But the parameter was never used.

Correct version:

```java
private String generateShortCodeWithUUID()
```

Reason:

The short code generation has nothing to do with the original URL.

UUID simply generates a random identifier.

---

# Important Learning

The input URL is **NOT** used while generating the short code.

Example:

```
Input:

google.com

↓

UUID.randomUUID()

↓

ab12cd
```

Second request:

```
facebook.com

↓

UUID.randomUUID()

↓

xy98mn
```

UUID never looks at the URL.

It only generates a random value.

---

# Then Why Does the Same URL Return the Same Short URL?

Because of duplicate URL prevention.

Flow:

```
google.com

↓

findByOriginalUrl()

↓

Already Exists

↓

Return existing short URL
```

The application never reaches UUID generation.

Therefore:

```
google.com

↓

ab12cd
```

is returned every time.

Not because UUID generated the same value.

But because we returned the already stored record.

---

# What Happens If Duplicate URL Prevention Is Removed?

Request 1:

```
google.com

↓

UUID

↓

ab12cd
```

Saved.

Request 2:

```
google.com

↓

UUID

↓

xy98mn
```

Saved.

Database becomes:

| Original URL | Short Code |
| ------------ | ---------- |
| google.com   | ab12cd     |
| google.com   | xy98mn     |

Now one URL has two different short URLs.

Usually we do not want this behaviour.

---

# Database-Level Protection

Application checks are not enough.

Added:

```java
@Column(unique = true)
private String shortCode;
```

This creates a UNIQUE constraint in the database.

Meaning:

```
No two rows can contain the same shortCode.
```

---

# Why Is Database Protection Needed?

Imagine two requests arriving simultaneously.

```
Application A             Application B

Generate ab12cd           Generate ab12cd

↓

Check DB

↓

Not Found                 Not Found

↓

Save                      Save
```

Both applications checked before either one saved.

Without a UNIQUE constraint:

Duplicate short codes could be inserted.

This situation is called a **Race Condition**.

Database constraints prevent this.

---

# Two Levels of Protection

Application Layer

```
Generate

↓

Check Database

↓

Generate Again if Needed
```

Database Layer

```
UNIQUE Constraint
```

Together:

```
Application Validation

+

Database Validation

=

Reliable System
```

This concept is called **Defense in Depth**.

Never rely on only one layer of validation.

---

# Random vs Hash-Based Generation

Current Approach:

```
google.com

↓

Random UUID

↓

ab12cd
```

Characteristics:

* Random
* Independent of input URL
* Can create different short URLs (if duplicate URL prevention is disabled)

---

Hash-Based Approach:

```
google.com

↓

Hash Function

↓

abc123
```

Characteristics:

* Deterministic
* Same input always produces the same output

Example:

```
google.com

↓

Hash

↓

abc123

google.com

↓

Hash

↓

abc123
```

---

# Why Most URL Shorteners Prefer Random IDs

Random IDs provide:

* Hard-to-predict URLs
* Independence from original URL
* Flexibility to create multiple short URLs for the same destination
* Simple implementation

Hash-based IDs always produce the same output for the same input, which is not always desirable.

---

# Concepts Learned

* UUID
* Collision
* Collision Prevention
* Duplicate URL Prevention
* Duplicate Short Code Prevention
* while Loop
* Helper Methods
* DRY Principle
* UNIQUE Constraint
* @Column(unique = true)
* Race Condition
* Defense in Depth
* Random vs Deterministic Generation
* Hash-Based URL Shortening

---

# Final Flow

```
Receive URL

↓

Already shortened?

↓

YES

↓

Return Existing Short URL

↓

NO

↓

Generate Random Short Code

↓

Already Exists?

↓

YES

↓

Generate Again

↓

NO

↓

Save to Database

↓

Return Short URL
```

---

# Key Takeaways

* Duplicate original URLs and duplicate short codes are different problems.
* UUID generation is random and does not depend on the input URL.
* A while loop is ideal when the number of retries is unknown.
* Extract repeated logic into helper methods.
* Never rely only on application logic for uniqueness.
* Database constraints provide the final guarantee of data integrity.
* Production systems use multiple layers of validation (Defense in Depth).
