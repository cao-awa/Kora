# Kora

> Currently, the software ecosystem of Kora is not complete.\
> You can consider this project an experimental JVM web server project.\
> The main focus is more FP, more control flow contextualization, and more zero-reflection design.

**Kora** is a high-performance, type-safe Kotlin web server framework built on Netty.

Kora treats HTTP APIs as **typed programs**, not runtime configurations.

Instead of assembling routing tables, annotations, and containers at runtime, Kora encourages developers to **describe
APIs as values**, composed through Kotlin expressions and verified as early as possible—preferably at compile time.

Kora is designed for developers who want correctness, predictability, and explicit behavior over implicit magic, only using the fewest reflection in launch stage to [bootstrap users code](https://github.com/cao-awa/Kora/blob/main/docs/entrypoint/README.md).

![](https://count.getloli.com/@@cao-awa.kora?name=%40cao-awa.kora&padding=7&offset=0&align=top&scale=1&pixelated=1&darkmode=auto)

# Use kora
Add these codes to your ``build.gradle`` file:
```groovy
repositories {
    maven {
        url 'https://jitpack.io'
    }
}

dependencies {
    implementation 'com.github.cao-awa:Kora:1.0.5'
}
```

And creating your Kora programs!

# What Kora Is

Kora is a **Kotlin-first**, expression-based web framework that emphasizes:

* Compile-time structure over runtime mutation
* Typed APIs over string-based routing
* Explicit control flow over annotation-driven behavior
* Reloadable graphs over global state

It is built on Netty for performance and IO efficiency and uses Kotlin coroutines as its execution model.

> In Kora, annotations never control routing, execution order, or lifecycle.
> When present, they are used only as **descriptive schema at data boundaries**.
>
> This guarantees that routing behavior and execution logic are always visible in code.

## What Makes Kora Different

Most web frameworks optimize for **runtime flexibility**.

Kora can also be flexible, but it primarily optimizes for **compile-time correctness** and **semantic clarity**.

* Routes are **values**, not side effects
* Parameters are **typed**, not string-based
* Handlers are **functions**, not magic containers
* **Reloading** in Kora is **graph replacement**, not class redefinition. Code is recompiled. The JVM is not mutated.

Kora is not a general-purpose application container.\
It does not manage object lifecycles or dependency graphs.\
Application structure is defined by Kotlin code, not framework containers.

It is a **language-shaped web framework**.

> In Ktor, routing mutates a global pipeline.\
> In Spring, routing is discovered via annotations.\
> In Kora, routing is an expression that produces a value.

# Quick Start
## No coding start
Use java command ``java -jar Kora-1.0.5.jar -server`` to run a Kora server with [Assets manager mode](#assets-manager-mode).

It can automatically response html or other files in assets path, or redirect path to "path/index.html" file.

## Test Cases

Some cases are written [here](https://github.com/cao-awa/Kora/blob/main/src/test/kotlin/Main.kt).

Here are some test cases:

### Case 1

Define and run a simple HTTP server with one route:

```kotlin
fun main() {
    // Define a required URL argument, get value in http scope.
    val actionArg = arg<Int>("action", missable = false)

    val http = http {
        route("/test") {
            get {
                // Get URL input argument.
                val action = actionArg(this)

                // Render HTML page.
                html {
                    head {
                        charset(Charsets.UTF_8)
                        viewport {
                            width(DEVICE_WIDTH)
                            initialScale(1.0)
                        }
                        pageTitle {
                            +"TestPage"
                        }
                    }
                    body {
                        a {
                            href("https://www.google.com")
                            text("Google")
                        }
                        p {
                            text("Successfully input arg '${actionArg.name}', value is '$action'")
                        }
                    }
                }
            }
        }
    }

    KoraHttpServer(http).start(
        port = 12345,
        useEpoll = true
    )
}
```

This starts an HTTP server on port `12345` with one route:

* `GET /test?action=1234`: `200 OK`
* `GET /test`: `400 BAD REQUEST`

HTML rendering is powered by [CapterTML](https://github.com/cao-awa/CaperTML), an HTML DSL library.

### Case 2

Define and run a simple HTTP server with only asset routes:

```kotlin
fun main() {
    val http = http {
        // Setup static assets path.
        assets("assets/")

        // Redirect all unregistered queries to the 404 page.
        ifAbort(HttpPathNotRegisteredException::class) {
            withAsset(redirectAsset = "error/404.html")
        }
    }

    KoraHttpServer(http).start(
        port = 12345,
        useEpoll = true
    )
}
```

## Case 3
Use Kora's libraries loader features, change the ``entrypoint`` key in your config to:
```json
"entrypoint": [
    "com.github.cao.awa.com.github.cao.awa.kora.redis.entrypoint.RedisPluginBootstrap#init",
    "com.github.cao.awa.kora.external.SampleEntrypoint#entry",
    "com.github.cao.awa.kora.entrypoint.KoraKotlinEntrypoint#entry"
]
```

Put ``libs/``(in this repo) to your jar location and use ``java -jar Kora-1.0.5.jar -server`` to launch Kora, you will see a  log ``External test success!``, if you deleted the libraries jar in ``{working_dir}/libs/``, this test wil got an error.

For details, see [Entrypoint](https://github.com/cao-awa/Kora/blob/main/docs/entrypoint/README.md) document.

You can also build your Kora programs, build jar and put it into ``{working_dir}/libs/``, and change the entrypoint config, make your special logics and services! 

## Structured Responses and HTTP Metadata

By default, Kora treats HTTP responses as **structured data**.

When a handler returns a Kotlin object, Kora serializes it and **injects HTTP metadata** into the response payload:

But Kora does not encourage embedding transport concerns into domain models.\
HTTP metadata injection is a transport-level concern and is configurable.

```json
{
  "type": "post",
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

HTTP metadata injection is configurable and can be disabled for stricter HTTP/body separation:

```kotlin
fun main() {
    // NOTE: Disable HTTP metadata injection ('instructHttpMetadata')
    // This will automatically disable status code and version injection.
    KoraHttpServer.instructHttpMetadata = false
    KoraHttpServer.instructHttpStatusCode = false
    KoraHttpServer.instructHttpVersionCode = false
}
```

Kora automatically serializes Kotlin data classes using [Cason](https://github.com/cao-awa/Cason), a lightweight, type-safe JSON/JSON5 library.

## Total Handlers and 204 No Content

A handler in Kora is a total function from request scope to a single response value.\
There is no such thing as a “partially constructed response” in Kora.\
It may describe response metadata, but it cannot partially construct a response.

To return `204 No Content`, use `NoContentResponse` explicitly:

```kotlin
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

Or if a return value is missing, Kora will automatically return a `204 NO CONTENT` response.

## Abort

Kora uses a scoped abort model where execution and error handling are strictly separated into non-overlapping lifetimes.

In Kora, aborting execution is not an exceptional case.\
It is a first-class, structured control flow with explicit scope boundaries.

The `abortWith()` or `abortIf()` methods define when to abort, and `.ifAbort { }` defines how aborted execution is rendered into a
response:

```kotlin
fun main() {
    val http = http {
        route("/test") {
            get {
                // Simulation code wrongs.
                abortWith(NullPointerException("Test if logic error occurs NPE"), HttpResponseStatus.BAD_REQUEST, this)
            }.ifAbort(NullPointerException::class) { exception ->
                LOGGER.error(exception)
                val httpProtocolVersion: HttpVersion = protocolVersion()
                LOGGER.error("Http protocol version: $httpProtocolVersion")
                val status: HttpResponseStatus = status()
                LOGGER.error("Http status: $status")
            }
        }
    }

    KoraHttpServer(http).start(
        port = 12345,
        useEpoll = true
    )
}
```

The client will receive data similar to:

```json
{
    "error_message": "Test if logic error occurs NPE",
    "stacktrace": [
        "java.lang.NullPointerException: Test if logic error occurs NPE",
        " - at MainKt.testError$lambda$0$0$0(Main.kt:68)",
        " - at com.github.cao.awa.kora.server.network.http.handler.KoraHttpRequestHandler.handle(KoraHttpRequestHandler.kt:42)",
        " - at com.github.cao.awa.kora.server.network.http.pipeline.KoraHttpRequestPipeline$handleFull$1.invokeSuspend$lambda$0(KoraHttpRequestPipeline.kt:107)",
        " - at com.github.cao.awa.kora.server.network.pipeline.KoraRequestPipeline.abortable(KoraRequestPipeline.kt:22)",
        " - at com.github.cao.awa.kora.server.network.http.pipeline.KoraHttpRequestPipeline$handleFull$1.invokeSuspend(KoraHttpRequestPipeline.kt:102)",
        " - at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:34)",
        " - at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:100)",
        " - at kotlinx.coroutines.internal.LimitedDispatcher$Worker.run(LimitedDispatcher.kt:124)",
        " - at kotlinx.coroutines.scheduling.TaskImpl.run(Tasks.kt:89)",
        " - at kotlinx.coroutines.scheduling.CoroutineScheduler.runSafely(CoroutineScheduler.kt:586)",
        " - at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.executeTask(CoroutineScheduler.kt:820)",
        " - at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.runWorker(CoroutineScheduler.kt:717)",
        " - at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.run(CoroutineScheduler.kt:704)"
    ],
    "internal_error_name": "Internal Server Error",
    "http_meta": {
        "http_version": "HTTP/1.1"
    },
    "error": "Server protocol (Kora/1.0.0, HTTP/1.1) error: Internal Server Error"
}
```

All abort scopes are copied from the source context, which Kora automatically collects.\
You can modify the scope data in the abort context.

## Assets manager mode
Use ``-jar Kora.jar`` to run Kora server will automatically running on assets manager mode, if kora running on assets manager mode, when url not fetch (such as ``http://127.0.0.1/test``), then Kora will automatically redirect to ``http://127.0.0.1/test/index.html``, if still not found, finally, it will got an error response,

Or write code like this and run the code: 
```kotlin
fun main() {
    val http = http {
        // Setup static asset path.
        assets("assets/")

        // Redirect all no registered query to 404 page.
        ifAbort(HttpPathNotRegisteredException::class) {
            withAsset(redirectAsset = "error/404.html")
        }
    }

    KoraHttpServer(http).start(
        port = 12345,
        useEpoll = true
    )
}
```

## PHP

Currently, Kora can execute PHP scripts through PHP-CGI, but support is incomplete and can currently only be used for single-file PHP scripts.

Simple sample:

```php
<?php

ob_clean();

echo '<!DOCTYPE html>
<html>
<head><meta charset="UTF-8"><title>CGI Connection test</title></head>
<body>
<h1>CGI Connection test success</h1>
<hr>
<h2>Server information</h2>
<ul>';

$cgi_vars = [
    'SERVER_SOFTWARE'   => 'Web server software',
    'GATEWAY_INTERFACE' => 'CGI Version',
    'REQUEST_METHOD'    => 'Request method',
    'QUERY_STRING'      => 'Query string',
    'SCRIPT_NAME'       => 'Script name',
    'PHP_SELF'          => 'PHP Script path',
];

foreach ($cgi_vars as $key => $desc) {
    $value = $_SERVER[$key] ?? 'No settings';
    echo "<li><strong>{$desc}：</strong> {$value}</li>";
}

echo '<li><strong>PHP Version: </strong> ' . PHP_VERSION . '</li>';
echo '</ul>';

echo '<details>
<summary>Click to view all $_SERVER Variables</summary>
<pre>';
print_r($_SERVER);
echo '</pre>
</details>';

echo '</body></html>';
```

# Performance

## Startup Time

Kora can launch an HTTP server within 200~500ms, even when creating a large route graph, because it uses native code instead of reflection scanning.

## Benchmark Test

Tested by [OHA](https://crates.io/crates/oha) on an `AMD Ryzen 7 8845HS`, Windows 10, with default settings: `100000`~`120000` HTTP requests per second. (min 109170, max 128003)

With JVM options (`-server -XX:+UseZGC -Xmx1G -Xms1G`)

Using the simplest test case:

```kotlin
fun main() {
    val html = html {
        head {
            charset(StandardCharsets.UTF_8)
        }
        body {
            p {
                +"Hello world!"
            }
        }
    }

    val http = http {
        route("/test") {
            get {
                html
            }
        }
    }

    KoraHttpServer(http).start(
        port = 45678,
        useEpoll = true
    )
}
```

Kora is not can only run in a 1G or more memory environment, it also can run in a 128M memory environment, although performance will be reduced to 50000~60000 HTTP requests per second. (min 57159, max 63148)

## Error benchmark

In the 1G memory case, if all requests result in errors (such as `404 Not Found`) instead of being correctly handled, performance will be reduced to 70000~80000 HTTP requests per second. (min 70545, max 80148)

In the 128M memory case, if all requests result in errors (such as `404 Not Found`) instead of being correctly handled, performance will be reduced to 20000~30000 HTTP requests per second. (min 21238, max 37494)

# Design Philosophy

## 1. Kotlin Is the Framework

Kora does not “support Kotlin”.

Kora is **designed for Kotlin**:

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

Annotations hide logic.\
Reflections hide cost.

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
