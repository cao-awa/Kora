# Custom entrypoint
Kora will generate a config file when it first startup, it seems like:
```json
{
    "print_config_details": true,
    "entrypoint": [
        "com.github.cao.awa.kora.server.network.http.entrypoint.KoraHttpServerEntrypoint#entry"
    ]
}
```

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
And build a  jar, and put it into ``{working_path}/libs/ `` path, and start Kora runtime, Kora will autoload your jars, don't use ``shadowJar``, shadowJar will pack useless classes that already provided by Kora jar.

The ``entrypoint`` config must declare full package name and class name or plugin name (See [plugin document](https://github.com/cao-awa/Kora/tree/main/docs/plugin/README.md)), use symbol '#' to split entrypoint method.

The entrypoint must receive a  ``KoraLaunchConfig`` instance or ``Array<String>`` argument or empty parameter, and must annotated by ``@JvmStatic``, it also must is an kotlin object instead of kotlin class.

The ``entry`` name can be other anything, just modify the name declare before '#' symbol.

Bootstrap will automatically invoke this entrypoint and executes your code.

## When missing
If ``entrypoint`` config are missing or defined to empty:
`````json
{
    "entrypoint": ""
}
`````

Kora will automatically reset it to ``com.github.cao.awa.kora.server.network.http.entrypoint.KoraHttpServerEntrypoint#entry``, an asset manager web server, you may need to configure the ``asset_path``, ``error_page`` and other configs in ``configs/kora_http.json``.

