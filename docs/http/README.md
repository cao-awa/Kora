# Basic usage

## Foreword

Don't use coroutine in request handler scope, it already running on coroutine scope, do this instead of improving
performance, it actually impacts the behaviors.

## Basic structure

```kotlin
object TestEntry {
    @JvmStatic
    fun entry() {
        val api = http {
            route("/test") {
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

## Request arguments

> Default your server launch on 12345 port here

The 'request argument' is the url contents after '?' such as 'http://127.0.0.1:12345/test?arg=1', the 'arg=1' is a
argument.

Define arg extractor and call it in request scope to get the argument value:

```kotlin
object TestEntry {
    @JvmStatic
    fun entry() {
        val input = arg<String>("input")
        val api = http {
            route("/test") {
                get {
                    val theInput = input(this)

                    // Return a result to response the request.
                    html {
                        head {
                            charset(StandardCharsets.UTF_8)
                        }
                        body {
                            p {
                                +"Input argument is $theInput"
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

The ``arg`` method has two arguments, first is url argument name, is required, second is missable flag, is optional, if
missable is true, Kora will return the default value of the type.

### Default value

When missable flag is true, Kora will get the usually default value, such as ``arg<Int>`` is 0, ``arg<String>`` is an
empty string, or ETC.

You can also setting custom default value manually:

```kotlin
object TestEntry {
    @JvmStatic
    fun entry() {
        val input = arg<String>("input", true).defaultValue("The input")
        // There same to above-mentioned.
    }
}
```

## Placeholders

The 'placeholder' just like its name, is a part in the url.

Define placeholder extractor and call it in request scope to get the placeholder value:

```kotlin
object TestEntry {
    @JvmStatic
    fun entry() {
        val placeholder = placeholder<String>("username")
        val api = http {
            route("/getUser/{username}") {
                get {
                    val username = placeholder(this)

                    // Return a result to response the request.
                    html {
                        head {
                            charset(StandardCharsets.UTF_8)
                        }
                        body {
                            p {
                                +"The user  is $username"
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

Unlike ``arg``, it only one name argument, it match to route defined placeholder, therefore, their names must be the
same.

## Delegate way

> Default your server launch on 12345 port here

```kotlin
object TestEntry {
    @JvmStatic
    fun entry() {
        val input by arg<Int>("input")
        val placeholder by placeholder<String>("test")

        val api = http {
            route("/test/{test}") {
                get {
                    html {
                        head {
                            charset(StandardCharsets.UTF_8)
                        }
                        body {
                            p {
                                +"Input arg 'input' is '$input', placeholder is '$placeholder'"
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

When you visit ``http://127.0.0.1:12345/test/awa?input=1234``, you will see a page shown
``Input arg 'input' is '1234', placeholder is 'awa'``

The delegate ``arg`` or ``placeholder`` can only access in request scope, cannot access in other locations, otherwise
Kora will throw a ``IllegalStateException`` to notice it.

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

And build method can only input most 7 args or placeholders, if your code ned more input, maybe you need to think is
there a problem with your design architecture?

Even you are using delegate(``by``) arg or placeholder, build method is still usable, you can use build like extractor
mode, because build not only ``build(TypedHttpArgument<T1>, TypedHttpArgument<T2> ... TypedHttpArgument<T7>, R)`` and
``build(TypedHttpUrlPlaceholder<T1>, TypedHttpUrlPlaceholder<T2> ... TypedHttpUrlPlaceholder<T7>, R)`` , it also
supports ``build(T1, T2 ... T7, R)``.

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
                }
            }
        }

        KoraHttpServer(api).start()
    }
}
```

This sample using ``arg``, it can also use in ``placeholder``, but cannot mix uses.

## Abort

Kora uses a scoped abort model where execution and error handling are strictly separated into non-overlapping lifetimes.

In Kora, aborting execution is not an exceptional case.\
It is a first-class, structured control flow with explicit scope boundaries.

The `abortWith()` or `abortIf()` methods define when to abort, and `.ifAbort { }` defines how aborted execution is
rendered into a
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

## Custom combinator

You can use ``combinator`` in ``arg`` or ``placeholder`` creating to define some custom combinate logics, you can have
multiple combinators instead of single combinator, just repeat call ``combinator`` method again.

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

# Assets manager mode

Use ``-jar Kora-{kora_version}.jar`` to run a Kora HTTP server will automatically running on assets manager mode, if
kora running on assets manager mode, when url not fetch (such as ``http://127.0.0.1/test``), then Kora will
automatically redirect to ``http://127.0.0.1/test/index.html``, if still not found, finally, it will get an error
response, you can modify ``error_page`` config in ``configs/kora_http.json`` config file to custom your 404 page,

The API code equivalent to:

```kotlin
http {
    // Setup static asset path.
    assets(assetManagerConfig.assetPath())

    // Redirect all no registered query to 404 page.
    ifAbort(HttpPathNotRegisteredException::class) {
        withAsset(redirectAsset = assetManagerConfig.errorPage())
    }
}
```

# PHP

Currently, Kora can execute PHP scripts through PHP-CGI, but support is incomplete and can currently only be used for
single-file PHP scripts.

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

# Structured Responses and HTTP Metadata

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

Kora automatically serializes Kotlin data classes using [Cason](https://github.com/cao-awa/Cason), a lightweight,
type-safe JSON/JSON5 library.

# Total Handlers and 204 No Content

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

# Performance

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

Kora is not can only run in a 1G or more memory environment, it also can run in a 128M memory environment, although
performance will be reduced.

|          | 1G                | 128M             |
|----------|-------------------|------------------|
| NIO      | 100000~120000 RPS | 50000~60000 RPS  |
| EPOLL    | waiting for test  | waiting for test |
| IO_URING | waiting for test  | waiting for test |

## Error benchmark

If all requests is fetched into errors (such as `404 Not Found`) instead of being correctly handled, performance will be
reduce.

|          | 1G               | 128M             |
|----------|------------------|------------------|
| NIO      | 70000~80000 RPS  | 20000~30000 RPS  |
| EPOLL    | waiting for test | waiting for test |
| IO_URING | waiting for test | waiting for test |