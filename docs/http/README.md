## Basic usage
```kotlin
object TestEntry {
    @JvmStatic
    fun entry() {
        val api = http {
            route("test") {
                get {
                    // Handle request here...
                    println("Something processed here")

                    // Return a result to response the request.
                    html {
                        head {
                            charset(StandardCharsets.UTF_8)
                        }
                        body {
                            p {
                                +"Hello world!"
                            }
                        }
                    }
                }
            }
        }
        
        KoraHttpServer(api).start()
    }
}
```

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
object TestEntry {
    @JvmStatic
    fun entry() {
        // NOTE: Disable HTTP metadata injection ('instructHttpMetadata')
        // This will automatically disable status code and version injection.
        KoraHttpServer.instructHttpMetadata = false
        KoraHttpServer.instructHttpStatusCode = false
        KoraHttpServer.instructHttpVersionCode = false
    }
}
```

Kora automatically serializes Kotlin data classes using [Cason](https://github.com/cao-awa/Cason), a lightweight, type-safe JSON/JSON5 library.

## Total Handlers and 204 No Content

A handler in Kora is a total function from request scope to a single response value.\
There is no such thing as a “partially constructed response” in Kora.\
It may describe response metadata, but it cannot partially construct a response.

To return `204 No Content`, use `NoContentResponse` explicitly:

```kotlin
object TestEntry {
    @JvmStatic
    fun entry() {
        val api = http {
            route("/test") {
                get {
                    NoContentResponse
                }
            }
        }

        KoraHttpServer(api).start()
    }
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
object TestEntry {
    @JvmStatic
    fun entry() {
        val http = http {
            route("/test") {
                get {
                    // Simulation code wrongs.
                    abortWith(
                        NullPointerException("Test if logic error occurs NPE"),
                        HttpResponseStatus.BAD_REQUEST,
                        this
                    )
                }.ifAbort(NullPointerException::class) { exception ->
                    LOGGER.error(exception)
                    val httpProtocolVersion: HttpVersion = protocolVersion()
                    LOGGER.error("Http protocol version: $httpProtocolVersion")
                    val status: HttpResponseStatus = status()
                    LOGGER.error("Http status: $status")
                }
            }
        }

        KoraHttpServer(http).start()
    }
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

## Typed arg and typed placeholder builder
Usually custom data class building ways:
```kotlin
object TestEntry {
    @JvmStatic
    fun entry() {
        val username = arg<String>("username", false)
        val password = arg<String>("password", false)

        data class LoginRequest(val username: String, val password: String)

        val api = http {
            route("test") {
                get {
                    val name = username(this)
                    val pws = password(this)
                    val loginRequest = LoginRequest(name, pws)
                    // Call auth plugin codes here...
                }
            }
        }

        KoraHttpServer(api).start()
    }
}
```

In Kora you can use ``build`` method to build your custom class in request scope, just input the args and constructor.

And build method can only input most 7 args or placeholders, if your code ned more input, maybe you need to think is there a problem with your design architecture?

```kotlin
object TestEntry {
    @JvmStatic
    fun entry() {
        val username = arg<String>("username", false)
        val password = arg<String>("password", false)

        data class LoginRequest(val username: String, val password: String)

        val api = http {
            route("test") {
                get {
                    val loginRequest = build(
                        username,
                        password,
                        // Use lambda constructor.
                        ::LoginRequest
                    )
                    // Call auth plugin codes here...

                    // Or:
                    // val loginRequest2 = build(
                    //    username,
                    //    password
                    // ) { name, pwd ->
                    //   // Construct manually.
                    //    LoginRequest(name, pwd)
                    // }
                }
            }
        }

        KoraHttpServer(api).start()
    }
}
```

it can also use in ``placeholder``, but cannot mix uses.

## Custom combinator
You can use ``combinator`` in ``arg`` or ``placeholder`` creating to define some custom combinate logics, you can have multiple combinators instead of single combinator, just repeat call ``combinator`` method again.

```kotlin
object TestEntry {
    @JvmStatic
    fun entry() {
        val username = arg<String>("username").combinator { content ->
            if (content.length < 5) {
                abortWith(
                    IllegalArgumentException("Username length must more than 5 characters"),
                    HttpResponseStatus.BAD_REQUEST,
                    this
                )
            }
            content
        }

        val api = http {
            route("/register") {
                get {
                    val name = username(this)
                    println("User $name registered")
                    html {
                        body {
                            p {
                                +"User $name registered now!"
                            }
                        }
                    }
                }
            }
        }

        KoraHttpServer(api).start()
    }
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

Tested by [OHA](https://crates.io/crates/oha) on an `AMD Ryzen 7 8845HS`, Windows 10, with default settings

With JVM options (`-server -XX:+UseZGC -Xmx1G -Xms1G`)

Using the simplest test case:

```kotlin
object TestEntry {
    @JvmStatic
    fun entry() {
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

        KoraHttpServer(http).start()
    }
}
```

Kora is not can only run in a 1G or more memory environment, it also can run in a 128M memory environment, although performance will be reduced.

|          | 1G               | 128M             |
|----------|------------------|------------------|
| NIO      | 100000~120000    | 50000~60000      |
| EPOLL    | waiting for test | waiting for test |
| IO_URING | waiting for test | waiting for test |

## Error benchmark

In the 1G memory case, if all requests result in errors (such as `404 Not Found`) instead of being correctly handled, performance will be reduce.

|          | 1G               | 128M             |
|----------|------------------|------------------|
| NIO      | 70000~80000      | 2000~30000       |
| EPOLL    | waiting for test | waiting for test |
| IO_URING | waiting for test | waiting for test |