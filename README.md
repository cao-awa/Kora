# Kora

> Currently, the software ecosystem of Kora is not complete.\
> You can consider this project an experimental JVM runtime project.\
> The main focus is more FP, more control flow contextualization, and more zero-reflection design.

**Kora** is a high-performance, type-safe, lifecycle-driven, effect-based JVM runtime for service execution, but currently focus on HTTP services.

Kora treats HTTP APIs as **typed programs**, not runtime configurations.

Instead of assembling routing tables, annotations, and containers at runtime, Kora encourages developers to **describe
APIs as values**, composed through Kotlin expressions and verified as early as possible—preferably at compile time.

Kora is designed for developers who want correctness, predictability, and explicit behavior over implicit magic, only using the fewest reflection in launch stage to [bootstrap users code](https://github.com/cao-awa/Kora/tree/main/docs/entrypoint/README.md).

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
    implementation 'com.github.cao-awa:Kora:{kora_version}'
}
```

For the versions, see [JitPack](https://jitpack.io/#cao-awa/Kora).

And creating your Kora programs!

# Quick Start
It can automatically response html or other files in assets path, or redirect path to "path/index.html" file.

Kora will generate a config file when it first startup, it seems like:
```json
{
    "print_config_details": true,
    "entrypoint": [
        "com.github.cao.awa.kora.server.network.http.entrypoint.KoraHttpServerEntrypoint#entry"
    ]
}
```

It means Kora will call the entrypoint ``com.github.cao.awa.kora.server.network.http.entrypoint.KoraHttpServerEntrypoint#entry``, this entrypoint method starts a asset manager http server (as mentioned above).

## No coding start
Use java command ``java -jar Kora-{kora_version}.jar -server`` to run a Kora server with [Assets manager mode](https://github.com/cao-awa/Kora/tree/main/docs/http/README.md#assets-manager-mode).

## With coding
You can also define your entrypoint, for details, see [entrypoint document](https://github.com/cao-awa/Kora/tree/main/docs/entrypoint/README.md).

Build your Kora programs, build jar and put it into ``{working_dir}/libs/``, and change the entrypoint config, make your special logics and services!

## HTTP server
See [HTTP document](https://github.com/cao-awa/Kora/tree/main/docs/http/README.md).

# What Kora Is

Kora is a **Kotlin-first**, expression-based JVM runtime that emphasizes:

* Compile-time structure over runtime mutation
* Typed APIs over string-based routing
* Explicit control flow over annotation-driven behavior
* Reloadable graphs over global state

It is built on Netty for performance and IO efficiency and uses Kotlin coroutines as its execution model.

> In Kora, annotations never control routing, execution order, or lifecycle.\
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
