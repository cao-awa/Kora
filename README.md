# Kora
**Kora** is a high performance, compile-time, type-safe Kotlin web server framework built on Netty, with out-and-out descriptive DSL.

It is a modern, Kotlin-first web server framework built on Netty, designed to treat HTTP APIs as **typed programs**, not runtime configurations.

Kora provides a **powerful expression-based DSL** for defining HTTP, RESTful, and WebSocket servers with **compile-time safety**, relying on **descriptive annotations only where necessary** and **minimizing runtime reflection**.
It embraces Kotlin’s language features—coroutines, inline functions, and type inference—to deliver an expressive, predictable, and reload-friendly development experience.

> In Kora, annotations never control routing, execution, or lifecycle.
When present, they serve purely as descriptive schema at data boundaries.

Kora intentionally avoids low-level concerns and focuses on **API expressiveness, correctness, and developer experience**.

## What Makes Kora Different

Most web frameworks optimize for flexibility at runtime.
Kora optimizes for **correctness at compile time**.

* Routes are **values**, not side effects
* Parameters are **typed**, not string-based
* Handlers are **functions**, not magic containers
* Reloading means **replacing graphs**, not restarting JVMs

Kora is not a general-purpose container framework.
It is a **language-shaped web framework**.

## Core Design Philosophy

### 1. Kotlin Is the Framework

Kora does not “support Kotlin.”
Kora is **designed for Kotlin**.

* Expression-based DSL instead of annotation metadata
* Inline and reified generics instead of reflection
* Coroutines as the default execution model
* Type inference as the primary API documentation

If an API cannot be expressed naturally in Kotlin syntax, it does not belong in Kora.

### 2. APIs Are Typed Programs

In Kora, an HTTP API is not a string-defined routing table.
It is a **typed program** with a known structure.

* Path segments carry types
* Query and body extraction is explicit and safe
* Handlers cannot access parameters that do not exist
* Invalid routes fail at compile time, not at runtime

Your handler signature *is* your contract.

### 3. Routing Is a Value

Routing in Kora produces a **route graph**, not side effects.

```kotlin
val api = routes {
    route("users", path<Int>("id")) {
        get { id -> User(id) }
    }
}
```

Because routes are values:

* They can be composed
* They can be tested
* They can be replaced at runtime
* They can be reloaded without restarting the server

### 4. Minimize Annotations and Reflection

Annotations hide logic.
Reflection hides cost.

Kora uses:

* Explicit DSLs
* Compile-time types
* Direct function calls

This makes behavior:

* Predictable
* Traceable
* Tool-friendly
* Reload-safe

### 5. Hot Reload Is a First-Class Feature

Kora does not restart the JVM on code changes.

Instead:

* The routing graph is rebuilt
* New requests use the new graph
* Existing requests complete normally

Hot reload is not a plugin—it is a natural consequence of the architecture.

### 6. Netty as a Foundation, Not a Surface

Kora is built on Netty, but Netty is not exposed.

Netty handles:

* Connections
* Protocols
* IO efficiency

Kora handles:

* Routing
* Typing
* Execution model
* Developer experience

You never write Netty code to use Kora.

## Design Tenets

* **Compile-time over runtime**
* **Explicit over implicit**
* **Values over side effects**
* **Language features over frameworks**
* **Reloadability over global state**
* **Small surface, strong guarantees**

## Non-Goals

Kora intentionally does **not** aim to be:

* A dependency injection container
* An annotation-driven meta framework
* A replacement for every Spring feature
* A low-level networking toolkit

Kora focuses on doing **one thing extremely well**:
**building safe, expressive, reloadable web servers in Kotlin.**

