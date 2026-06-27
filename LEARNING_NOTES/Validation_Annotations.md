# Validation Annotations

## Objective

Validate incoming request data automatically using Spring Boot Validation instead of writing manual validation code.

---

# Before Validation

Initially, validation was performed manually inside the Service layer.

```java
if(inputurl == null || inputurl.isBlank()){
    throw new InvalidUrlException("URL cannot be empty");
}
```

Problems:

* Manual checks everywhere
* Repetitive code
* Difficult to maintain
* Service layer mixed validation with business logic

---

# Spring Boot Validation

Spring Boot provides Bean Validation.

Instead of manually checking values, we describe validation rules using annotations.

Example:

```java
@NotBlank(message = "URL cannot be empty")
private String url;
```

This tells Spring:

"The url field must not be null, empty, or contain only spaces."

---

# @Valid

Validation is triggered using:

```java
@Valid
@RequestBody UrlRequestDTO request
```

When Spring sees `@Valid`, it validates the DTO before the controller method executes.

---

# Request Flow

```text
Client
    │
    ▼
HTTP Request
    │
    ▼
@RequestBody
    │
    ▼
@Valid
    │
    ├────────────► Validation Failed
    │                   │
    │                   ▼
    │      MethodArgumentNotValidException
    │                   │
    │                   ▼
    │      GlobalExceptionHandler
    │
    ▼
Controller
    │
    ▼
Service
```

Important:

If validation fails, the controller and service are **never executed**.

---

# @NotBlank vs @NotNull

## @NotNull

Checks only whether the value is null.

Allowed:

```text
""
```

```text
"   "
```

Not Allowed:

```text
null
```

---

## @NotBlank

Checks:

* Not null
* Not empty
* Not only whitespace

Allowed:

```text
"https://google.com"
```

Not Allowed:

```text
null
```

```text
""
```

```text
"    "
```

For URLs, `@NotBlank` is the correct choice.

---

# Validation Exception

When validation fails, Spring automatically throws:

```text
MethodArgumentNotValidException
```

We handle this inside our Global Exception Handler.

---

# Returning Custom Messages

Instead of returning Spring's default validation error, we extract the message defined in the annotation.

Example:

```java
@NotBlank(message = "URL cannot be empty")
```

Inside the exception handler:

```java
ex.getBindingResult()
  .getFieldError()
  .getDefaultMessage();
```

This returns:

```text
URL cannot be empty
```

instead of a long internal Spring error message.

---

# Why Use Validation Annotations?

Advantages:

* Cleaner code
* Less boilerplate
* Automatic validation
* Consistent error handling
* Better readability
* Business logic remains inside the Service layer

---

# What I Learned

* Bean Validation
* Validation Annotations
* @Valid
* @NotBlank
* MethodArgumentNotValidException
* BindingResult
* FieldError
* Default Validation Messages
* Automatic Request Validation
* Separation of Validation and Business Logic

---

# Must Memorize (Interview)

Annotations:

* @Valid
* @NotBlank
* @NotNull
* @Size
* @Pattern
* @Email

Exception:

* MethodArgumentNotValidException

Useful method chain:

```java
ex.getBindingResult()
  .getFieldError()
  .getDefaultMessage();
```

Understand the flow rather than memorizing every method individually.

---

# Key Takeaways

* Validation belongs close to the data (DTO).
* Business logic belongs in the Service layer.
* Spring performs validation before entering the controller.
* Validation failures automatically become exceptions.
* Global Exception Handler creates consistent error responses.
* Validation annotations reduce boilerplate and improve maintainability.
