# Kora

**Kora** is a high performance, compile-time, type-safe Kotlin web server framework built on Netty, with out-and-out
descriptive DSL.

It is a modern, Kotlin-first web server framework built on Netty, designed to treat HTTP APIs as **typed programs**, not
runtime configurations.

Kora provides a **powerful expression-based DSL** for defining HTTP, RESTful, and WebSocket servers with **compile-time
safety**, relying on **descriptive annotations only where necessary** and **minimizing runtime reflection**.
It embraces Kotlin’s language features ```coroutines```, ```inline``` functions, and type inference to deliver an
expressive, predictable, and reload-friendly development experience.

> In Kora, annotations never control routing, execution, or lifecycle.
> When present, they serve purely as descriptive schema at data boundaries.

Kora intentionally avoids low-level concerns and focuses on API expressiveness, correctness, and developer experience.

## What Makes Kora Different

Most web frameworks optimize for flexibility at runtime.
Kora optimizes for **correctness at compile time**.

* Routes are **values**, not side effects
* Parameters are **typed**, not string-based
* Handlers are **functions**, not magic containers
* Reloading means **replacing graphs**, not restarting JVMs

Kora is not a general-purpose container framework.
It is a **language-shaped web framework**.

## Quick start
Build and run a simple HTTP server with two routes:

```kotlin
import com.github.cao.awa.kora.server.network.http.KoraHttpServer
import com.github.cao.awa.kora.server.network.http.builder.server
import io.netty.handler.codec.http.HttpResponseStatus

fun main() {
    val api = server {
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

When run, this starts an HTTP server on port `12345` with two routes:
1. ```post``` get 200 OK
2. ```get``` got 500 Internal server error).

Kora will auto do serializes for data class via [Cason](https://github.com/cao-awa/Cason).

And the HTTP client will get data that like:

```json
{
  "type": "post",
  "timestamp": 1700000000000,
  "http_status": 200
}
```

By default, Kora treats HTTP responses as structured data.
When returning a Kotlin object from a handler, Kora serializes it using Cason and injects the HTTP status code into the serialized output.

The response body is the returned value, optionally augmented with HTTP metadata by the runtime.

This behavior provides a unified and debuggable response model and can be disabled via configuration for stricter HTTP/body separation:

```kotlin
fun main() {
    KoraHttpServer.instructHttpStatusCode = false
}
```

## Design Philosophy

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
fun main() {
    val api = server {
        route("/test") {
            post { /* */ }

            get { /* */ }
        }
    }

    KoraHttpServer(api).start(
        port = 12345,
        useEpoll = true
    )
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

### 5. Hot Reload
> Not done yet.

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

