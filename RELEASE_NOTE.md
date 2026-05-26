## Breaking changes
### Architecture
Kora is not a webserver now, Kora is a JVM runtime now.

Resident services like http server must call ``KoraStatus.registerLifecycle``,  ``KoraStatus.registerReloadListener``, ``KoraStatus.registerStopListener``, and when stopped, must call ``KoraStatus.completedLifecyle``.

If plugin is only simple logics, don't register the lifecycles.

### Duties separation
The config ``launch.json`` don't contain ``asset_manager`` and ``netty`` or other network configurations now, they moved to new config file ``kora_http.json``:
```json
{
    "server_host": "0.0.0.0",
    "server_port": 12345,
    "asset_manager": {
        "error_page": "error/404.html",
        "enable": true,
        "asset_path": "assets/"
    },
    "netty": {
        "io": "epoll",
        "snd_buffer": 65536,
        "reuse_address": true,
        "backlog": 8192,
        "allocator": "default",
        "keep_alive": true,
        "rcv_buffer": 65536,
        "tcp_no_delay": true
    }
}
```

## IO
Support to ``io_uring`` IO module.

## Netty
Add ``snd_buffer`` option to set sending buffer.

## Typed arg and typed placeholder builder
Usually custom data class building ways:
```kotlin
fun testBuild() {
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

    KoraHttpServer(api).start(
        port = 12345
    )
}
```

In Kora you can use ``build`` method to build your custom class in request scope, just input the args and constructor.

And build method can only input most 7 args or placeholders, if your code ned more input, maybe you need to think is there a problem with your design architecture?

```kotlin
fun testBuild() {
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

    KoraHttpServer(api).start(
        port = 12345
    )
}
```

it can also use in ``placeholder``, but cannot mix uses.

## Custom combinator
The ``arg`` and ``placeholder`` can use custom combinator now:
```kotlin
fun testCombinator() {
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

    KoraHttpServer(api).start(
        port = 12345
    )
}
```
It also can have multiple combinators instead of single combinator, just repeat call ``combinator`` method again.
