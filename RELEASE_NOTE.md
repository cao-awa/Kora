# Plugin

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