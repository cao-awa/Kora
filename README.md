# Kora

**Kora** is a high-performance, type-safe Kotlin web server framework built on Netty.

Kora treats HTTP APIs as **typed programs**, not runtime configurations.

Instead of assembling routing tables, annotations, and containers at runtime, Kora encourages developers to **describe
APIs as values**, composed through Kotlin expressions and verified as early as possible—preferably at compile time.

Kora is designed for developers who want correctness, predictability, and explicit behavior over implicit magic.

![](https://count.getloli.com/@@cao-awa.kora?name=%40cao-awa.kora&padding=7&offset=0&align=top&scale=1&pixelated=1&darkmode=auto)

# What Kora Is

Kora is a **Kotlin-first**, expression-based web framework that emphasizes:

* Compile-time structure over runtime mutation
* Typed APIs over string-based routing
* Explicit control flow over annotation-driven behavior
* Reloadable graphs over global state

It is built on Netty for performance and IO efficiency, and uses Kotlin coroutines as its execution model.

> In Kora, annotations never control routing, execution order, or lifecycle.
> When present, they are used only as **descriptive schema at data boundaries**.
>
> This guarantees that routing behavior and execution logic are always visible in code.

## What Makes Kora Different

Most web frameworks optimize for **runtime flexibility**.

Kora also can be flexible, but it primarily optimizes for **compile-time correctness** and **semantic clarity**.

* Routes are **values**, not side effects
* Parameters are **typed**, not string-based
* Handlers are **functions**, not magic containers
* **Reloading** in Kora is a **graph replacement**, not class redefinition. Code is recompiled. The JVM is not mutated.

Kora is not a general-purpose application container.\
It does not manage object lifecycles or dependency graphs.\
Application structure is defined by Kotlin code, not framework containers.

It is a **language-shaped web framework**.

> In Ktor, routing mutates a global pipeline.\
> In Spring, routing is discovered via annotations.\
> In Kora, routing is an expression that produces a value.
> 
# Quick Start

Define and run a simple HTTP server with two routes:

```kotlin
import com.github.cao.awa.kora.server.network.http.KoraHttpServer
import com.github.cao.awa.kora.server.network.http.builder.server
import io.netty.handler.codec.http.HttpResponseStatus

fun main() {
    val api = http {
        route("/test") {
            post {
                KoraResponse(
                    type = "post",
                    timestamp = System.currentTimeMillis()
                )
            }

            get {
                status = HttpResponseStatus.INTERNAL_SERVER_ERROR

                KoraResponse(
                    type = "get",
                    timestamp = System.currentTimeMillis()
                )
            }
        }
    }

    KoraHttpServer(api).start(
        port = 12345,
        useEpoll = true
    )
}

data class KoraResponse(
    val type: String,
    val timestamp: Long
)
```

This starts an HTTP server on port `12345` with two routes:

* `POST /test` → `200 OK`
* `GET /test` → `500 Internal Server Error`

Kora automatically serializes Kotlin data classes using
[Cason](https://github.com/cao-awa/Cason), a lightweight, type-safe JSON/JSON5 library.

## Structured Responses and HTTP Metadata

By default, Kora treats HTTP responses as **structured data**.

When a handler returns a Kotlin object, Kora serializes it and **instructs HTTP metadata** into the response payload:

But Kora does not encourage embedding transport concerns into domain models.\
HTTP metadata instruction is a transport-level concern and is configurable.

```json
{
  "type": "post",
  "timestamp": 1700000000000,
  "http_meta": {
    "http_version": "HTTP/1.1",
    "http_status": 200
  }
}
```

This unified response model allows:

* Non-HTTP clients (CLI tools, MQ consumers, test harnesses) to consume responses directly
* Easier debugging and inspection
* Transport-agnostic result handling

Transport metadata is always derived from the response description.\
It never influences handler semantics.

HTTP metadata instruction is configurable and can be disabled for stricter HTTP/body separation:

```kotlin
fun main() {
    // NOTE: Disable HTTP metadata instruct ('instructHttpMetadata')
    // will auto disabled status code and version instruction.
    KoraHttpServer.instructHttpMetadata = false
    KoraHttpServer.instructHttpStatusCode = false
    KoraHttpServer.instructHttpVersionCode = false
}
```

## Total Handlers and 204 No Content

A handler in Kora is a total function from request scope to a single response value.\
There is no such thing as a “partially constructed response” in Kora.\
It may describe response metadata, but it cannot partially construct a response.

A handler must always produce an explicit response.
Missing return values or “status-only” handlers are rejected.

To return `204 No Content`, use `NoContentResponse` explicitly:

```kotlin
import com.github.cao.awa.kora.server.network.http.KoraHttpServer
import com.github.cao.awa.kora.server.network.http.builder.server
import io.netty.handler.codec.http.HttpResponseStatus

fun main() {
    val api = http {
        route("/test") {
            get {
                status = HttpResponseStatus.NO_CONTENT
                NoContentResponse
            }
        }
    }

    KoraHttpServer(api).start(
        port = 12345,
        useEpoll = true
    )
}
```

This design ensures:

* Every handler has a clear, explicit outcome
* No ambiguous or partially-defined responses
* Stronger guarantees about API behavior

## Abort

Kora uses a scoped abort model where execution and error handling are strictly separated into non-overlapping lifetimes.

In Kora, aborting execution is not an exceptional case.\
It is a first-class, structured control flow with explicit scope boundaries.

The ```abortWith()``` or ```abortIf()``` defines when to abort, and ```.abort {}``` defines how aborted execution is rendered into a
response:

```kotlin
import com.github.cao.awa.kora.server.network.http.KoraHttpServer
import com.github.cao.awa.kora.server.network.http.builder.server
import io.netty.handler.codec.http.HttpResponseStatus

fun main() {
    val api = http {
        route("/test") {
            get {
                // Abort this scope, into next scope 'abort'.
                abortWith(HttpResponseStatus.INTERNAL_SERVER_ERROR)
            }.abort {
                KoraErrorResponse(
                    "Error details",
                    status().code(),
                    System.currentTimeMillis()
                )
            }
        }
    }

    KoraHttpServer(api).start(
        port = 12345,
        useEpoll = true
    )
}

data class KoraErrorResponse(
    val error: String,
    val code: Int,
    val timestamp: Long
)
```

Client will get data seems like:

```json
{
  "code": 500,
  "error": "Error details",
  "timestamp": 1768148489124,
  "http_meta": {
    "http_version": "HTTP/1.1",
    "http_status": 500
  }
}
```

All abort scope is readonly-scope from contexts Kora auto collecting.\
Cannot modifying the scope data in abort context.

Response body is also required in abort scope, so that every abort has a clear, explicit outcome.

# Performance
Test by JMeter on ```AMD Ryzen 7 8845HS w```, Windows 10, with default settings: ```190000```~```210000``` HTTP requests per second.

# Design Philosophy

## 1. Kotlin Is the Framework

Kora does not “support Kotlin”.

Kora is **designed for Kotlin**.

* Expression-based DSLs instead of annotation metadata
* Inline and reified generics instead of reflection
* Coroutines as the default execution model
* Type inference as primary documentation

If an API cannot be expressed naturally in Kotlin syntax, it does not belong in Kora.

## 2. APIs Are Typed Programs

In Kora, an HTTP API is not a string-defined routing table.

It is a **typed program** with a known structure.

* Path segments carry types
* Parameters are explicitly declared
* Handlers cannot access data that does not exist
* Invalid routes fail early, not silently at runtime

Your handler signature **is** your contract.

## 3. Routing Is a Value

Routing in Kora produces a **route graph**, not side effects.

```kotlin
val api = http {
    route("/test") {
        post { /* ... */ }
        get { /* ... */ }
    }
}
```

Because routes are values:

* They can be composed
* They can be tested
* They can be replaced
* They can be reloaded without restarting the JVM

## 4. Minimize Annotations and Reflection

Annotations hide logic.
Reflection hides cost.

Kora relies on:

* Explicit DSLs
* Compile-time types
* Direct function calls

This makes behavior:

* Predictable
* Traceable
* Tool-friendly
* Reload-safe

## 5. Hot Reload

> *Planned*

Kora does not reload by restarting the JVM.

Instead:

* The routing graph is rebuilt
* New requests use the new graph
* Existing requests complete normally

Hot reload is not a plugin—it is a consequence of treating routes as values.

The initial implementation focuses on fast graph replacement without JVM restart.\
More advanced class-level reloading is intentionally out of scope.

## 6. Netty as a Foundation, Not a Surface

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

# Design Tenets

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

> **Building safe, expressive, reloadable web servers in Kotlin.**

