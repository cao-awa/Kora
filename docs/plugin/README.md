# Custom plugin

Kora allows plugin load, you can only depend on Kora, and write code:

```kotlin
package com.xxx.plugin.entry

import com.github.cao.awa.kora.launch.config.KoraLaunchConfig

object TestPlugin {
    @JvmStatic
    fun entry(config: KoraLaunchConfig) {
        // Write your codes here!
    }
}
```

and write plugin metadata in ``META-INF/plugin.json``:

```json
{
    "name": "test-plugin",
    "entrypoint": "com.xxx.plugin.entry.TestPlugin#entry"
}
```

then build jar and put into ``libs/`` directory, your plugin can be automatically load by Kora, your codes in ``entry``
method will be executed.

Certainly, you also need to define your plugin in ``launch.json``, you can use your plugin name or full method location
to define entrypoint:

```json
{
    "entrypoint": [
        "test-plugin"
    ]
}
```

or

```json
{
    "entrypoint": [
        "com.xxx.plugin.entry.TestPlugin#entry"
    ]
}
```

## Method requirement

The plugin entrypoint must in a kotlin object class instead of kotlin class, and must annotate with ``@JvmStatic``,
optionally, input arg can choose 3 different way:

```kotlin
package com.xxx.plugin.entry

import com.github.cao.awa.kora.launch.config.KoraLaunchConfig

object TestPlugin {
    @JvmStatic
    fun entrypoint1(config: KoraLaunchConfig) {
        // First choose, got a launching config.
    }

    @JvmStatic
    fun entrypoint2(args: Array<String>) {
        // Second choose, got the main entrypoint args.
    }

    @JvmStatic
    fun entrypoint3() {
        // Third choose, no anything input.
    }
}
```

If method doesn't match to anyone entrypoint way, Kora cannot execute your codes.

## Depends

If your plugin is depended on other plugins, you can write ``depends_on`` in metadata:

```json
{
    "name": "test-plugin",
    "entrypoint": "com.xxx.plugin.entry.TestPlugin#entry",
    "depends_on": [
        "other-plugin-1",
        "other-plugin-2"
    ]
}
```

All depend on plugins must load before your plugin, correctly define:

```json
{
    "entrypoint": [
        "other-plugin-1",
        "other-plugin-2",
        "test-plugin"
    ]
}
```

Incorrectly:

```json
{
    "entrypoint": [
        "test-plugin",
        "other-plugin-1",
        "other-plugin-2"
    ]
}
```

## Fallback
> NOTICE:\
> A plugin can only take once fallback execution, if fallback still errors, Kora will fast-fall shutdown.

You can define a fallback method in plugin metadata, when depend on plugins doesn't loaded or your plugin code got an
exception, Kora will execute the fallback path:

```json
{
    "name": "test-plugin",
    "entrypoint": "com.xxx.plugin.entry.TestPlugin#entry",
    "fallback": "com.xxx.plugin.entry.TestPlugin#fallback"
}
```

And you must ensure the fallback method is present and satisfy the requirements.

Sample:

```kotlin
package com.xxx.plugin.entry

import com.github.cao.awa.kora.launch.config.KoraLaunchConfig

object TestPlugin {
    @JvmStatic
    fun entry(config: KoraLaunchConfig) {
        throw RuntimeException("Error in main entrypoint")
    }

    fun fallback(config: KoraLaunchConfig) {
        // Handle others code logic here.
    }
}
```

If stage into fallback path, you can use ``KoraLaunchConfig.error()`` got the exception that last time happened, it may help to complete fallback logics.

Or use a special signature method, receive a ``Throwable`` instance to got the exception:
```kotlin
package com.xxx.plugin.entry

import com.github.cao.awa.kora.launch.config.KoraLaunchConfig

object TestPlugin {
    @JvmStatic
    fun entry(config: KoraLaunchConfig) {
        throw RuntimeException("Error in main entrypoint")
    }

    fun fallback(cause: Throwable) {
        // Handle others code logic here.
    }
}
```

## Repeat definition
Plugin cannot define twice or more times in ``entrypoint``, it cause fast-fall directly.