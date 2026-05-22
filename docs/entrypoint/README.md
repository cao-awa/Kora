# Custom entrypoint
You can declare an entrypoint in config with ``entrypoint`` key:
```json
{
    "entrypoint": [
        "com.github.xxx.entry.SampleEntrypoint#entry"
    ]
}
```

And writes code like this:

```kotlin
package com.github.xxx.entry

import com.github.cao.awa.kora.launch.config.KoraLaunchConfig
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object SampleEntrypoint {
    private val LOGGER: Logger = LogManager.getLogger("SampleEntrypoint")

    @JvmStatic
    fun entry(launchConfig: KoraLaunchConfig) {
        LOGGER.info("External plugin test success!")
    }
}
```

> NOTICE:\
> If you are using the libraries that aren't Kora integrated,\
> Then you must put the library build jar into the '{working_path}/libs/' path.

And build a  jar, and put it into ``{working_path}/libs/ `` path, don't use shadow jar, shadow jar will pack useless classes that already provided by Kora jar.

The ``entrypoint`` config must declare full package name and class name, use symbol '#' to split entrypoint method.

Kora bootstrap must receive a  ``KoraLaunchConfig`` instance or ``Array<String>`` argument or empty parameter, and must annotated by ``@JvmStatic``, it also must is an kotlin object instead of kotlin class.

The ``entry`` name can be other anything, just modify the name declare before '#' symbol.

Bootstrap will automatically invoke this entrypoint and executes your code.

## When missing
If ``entrypoint`` config are missing or defined to empty:
`````json
{
    "entrypoint": ""
}
`````

Kora will automatically start an asset manager web server, you may need to configure the ``asset_path``, ``error_page`` and other configs.

## Multi entrypoint
Clone Kora's repo in your IDE, and run the ``src/test/kotlin/Main.kt`` file, Kora's repo contains these plugins in path ``libs/``, you will got multiple plugins test output and finally run the Kora default asset manager web server:

```json
{
    "entrypoint": [
        "com.github.cao.awa.com.github.cao.awa.kora.redis.entrypoint.RedisPluginBootstrap#init",
        "com.github.cao.awa.kora.external.SampleEntrypoint#entry",
        "com.github.cao.awa.kora.entrypoint.KoraKotlinEntrypoint#entry"
    ]
}
```
