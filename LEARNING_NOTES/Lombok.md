# Lombok

## What is Lombok?

Lombok is a Java library that reduces boilerplate code by generating common methods automatically during compilation.

Instead of manually writing:

* Getters
* Setters
* Constructors
* Builder
* `toString()`
* `equals()`
* `hashCode()`

Lombok generates them using annotations.

---

# Why use Lombok?

Without Lombok:

```java
public class User {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
```

With Lombok:

```java
@Getter
@Setter
public class User {
    private String name;
}
```

Less code, easier to read and maintain.

---

# How Lombok Works

Lombok works during **compile time**.

Compilation flow:

```
Java Source (.java)
        │
        ▼
Lombok Annotation Processor
        │
        ▼
Java Compiler (javac)
        │
        ▼
Bytecode (.class)
```

Lombok generates Java code before the compiler creates the `.class` file.

At runtime, there is **no Lombok**.

---

# Annotation Processing

Annotations like:

```java
@Getter
@Builder
@Entity
```

are just metadata.

An **Annotation Processor** reads these annotations and generates additional Java code.

Therefore, IntelliJ must have:

```
Settings
→ Build, Execution, Deployment
→ Compiler
→ Annotation Processors
→ Enable Annotation Processing
```

Otherwise the IDE may show errors like:

```
Cannot resolve method getOriginalUrl()
```

even though the project compiles.

---

# Lombok Annotations Learned

## @Getter

Generates getter methods.

```java
@Getter
private String originalUrl;
```

Generates:

```java
public String getOriginalUrl() {
    return originalUrl;
}
```

---

## @Setter

Generates setter methods.

```java
@Setter
private String originalUrl;
```

Generates:

```java
public void setOriginalUrl(String originalUrl) {
    this.originalUrl = originalUrl;
}
```

---

## @NoArgsConstructor

Generates:

```java
public UrlMappingEntity() {}
```

### Why?

Hibernate/JPA requires a no-argument constructor to create entity objects when reading data from the database.

---

## @AllArgsConstructor

Generates a constructor containing every field.

---

## @Builder

Creates objects using the Builder Pattern.

Instead of:

```java
UrlMappingEntity entity = new UrlMappingEntity();
entity.setOriginalUrl(url);
entity.setShortCode(code);
```

Use:

```java
UrlMappingEntity entity = UrlMappingEntity.builder()
        .originalUrl(url)
        .shortCode(code)
        .build();
```

### Advantages

* Cleaner code
* More readable
* No confusion with constructor parameter order
* Reduces partially initialized objects

---

# @Builder.Default

Field initialization does **NOT** work automatically with `@Builder`.

Incorrect:

```java
private Long clickCount = 0L;
```

Builder may create:

```
clickCount = null
```

Correct:

```java
@Builder.Default
private Long clickCount = 0L;
```

This preserves the default value when using the builder.

---

# Primitive vs Wrapper

Primitive:

```java
long
int
boolean
```

* Cannot be null
* Has default values

Wrapper:

```java
Long
Integer
Boolean
```

* Can be null
* Used frequently with JPA

---

# Why use Long for Entity IDs?

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

Before saving:

```
id = null
```

After saving:

```
id = 1
```

Using `Long` allows Hibernate to determine whether an entity has already been persisted.

If `long` were used, the default value would be `0`, making it harder to distinguish a new entity from one with an assigned ID.

---

# Best Practices

✔ Use:

```java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
```

instead of `@Data` for JPA entities.

✔ Use:

```java
@Builder.Default
```

for fields with default values.

✔ Prefer `Long` for entity IDs.

---

# Entity Used in Our Project

```java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "url_mappings")
public class UrlMappingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String originalUrl;

    @Column(nullable = false, unique = true)
    private String shortCode;

    @Builder.Default
    private Long clickCount = 0L;

    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;
}
```

---

# Key Takeaways

* Lombok reduces boilerplate code.
* Lombok works at compile time through annotation processing.
* Annotation Processing must be enabled in the IDE.
* `@Builder` improves object creation readability.
* Use `@Builder.Default` for default field values.
* Use `Long` for JPA IDs because `null` represents an unsaved entity.
* Prefer explicit Lombok annotations over `@Data` for JPA entities.
