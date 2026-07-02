# Kora

> Kora is still evolving.
> The runtime and HTTP stack are already usable for real projects, while the surrounding ecosystem is still under active development.\
> Kora focuses on functional programming, structured control flow, minimal reflection.

Kora is a Kotlin-first web runtime for developers who prefer
explicit code over annotations and compile-time safety over runtime magic.

Instead of discovering routes through reflection,
Kora lets you build APIs as ordinary Kotlin values:

* No annotation routing.
* Minimal reflection.
* Type-safe request extraction.
* Structured control flow.

Kora is designed for developers who want correctness, predictability, and explicit behavior over implicit magic, only using reflection during startup to [bootstrap user code](https://github.com/cao-awa/Kora/tree/main/docs/entrypoint/README.md).

![](https://count.getloli.com/@@cao-awa.kora?name=%40cao-awa.kora&padding=7&offset=0&align=top&scale=1&pixelated=1&darkmode=auto)

# Why Kora?
Kora lets you write services as ordinary Kotlin code.

Instead of annotations, reflection and runtime routing discovery,
everything is defined as explicit Kotlin values.

That means:

* Routes are Kotlin values, not runtime registrations.
* Parameters are extracted with types, not strings.
* Control flow is explicit through abort scopes.

## Hot Reload

Kora supports reloading without restarting the JVM by **replacing the route graph**, not by redefining loaded classes.

This requires the application to be **recompiled** after code changes. Once a new graph is built, Kora atomically swaps it into service:

* Existing requests continue using the old graph until completion.
* New requests are dispatched to the new graph immediately.
* The JVM process itself is never restarted.

This approach keeps reload semantics explicit and predictable while avoiding JVM class redefinition.

# Getting started
Add codes to your ``build.gradle`` file:
```groovy
repositories {
    maven {
        url 'https://jitpack.io'
    }
}

dependencies {
    implementation 'com.github.cao-awa:Kora:{kora_version}'
}
```

For the versions, see [JitPack](https://jitpack.io/#cao-awa/Kora).

Now you're ready to build your first Kora application.

# Quick Start
Use ``./gradlew run`` to launch Kora quickly, Kora will generate a config file on first startup, it seems like:

```json
{
    "print_config_details": true,
    "entrypoint": [
        "com.github.cao.awa.kora.server.network.http.entrypoint.KoraHttpServerEntrypoint#entry"
    ]
}
```

It means Kora will call the entrypoint ``com.github.cao.awa.kora.server.network.http.entrypoint.KoraHttpServerEntrypoint#entry``, this entrypoint method starts an asset manager http server (as mentioned above).

It can automatically serve HTML or other files in assets path, or redirect path to "path/index.html" file.

If you are using gradle task to launch Kora and want to run your codes, you also need to change the entrypoint configuration, please see [entrypoint document](https://github.com/cao-awa/Kora/tree/main/docs/entrypoint/README.md).

Put jars into ``{working_dir}/libs/``, and change the entrypoint config, build your own services.

## Development environment 
For details, see [entrypoint document](https://github.com/cao-awa/Kora/tree/main/docs/entrypoint/README.md).

### HTTP server
Example code:
```kotlin
@JvmStatic
fun entry() {
    val username by arg<String>("username")

    val api = http {
        route("/hello") {
            get {
                // Response rendered HTML page.
                html {
                    body {
                        p {
                            +"Hello $username!"
                        }
                    }
                }
            }
        }
    }

    KoraHttpServer(api).start()
}
```

Launch Kora and open ``http://127.0.0.1:12345/hello?username=Kora`` to view the hello page.

For details, see [HTTP document](https://github.com/cao-awa/Kora/tree/main/docs/http/README.md).

## Production environment 
Use java command ``java -jar Kora-{kora_version}.jar -server`` to run a Kora http server with [Assets manager mode](https://github.com/cao-awa/Kora/tree/main/docs/http/README.md#assets-manager-mode).

# CLI command
See [command document](https://github.com/cao-awa/Kora/tree/main/docs/command/README.md).
