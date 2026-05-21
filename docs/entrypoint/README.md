# Custom entrypoint
You can declare an entrypoint in config with ``entrypoint`` key:
```json
{
    "entrypoint": "com.github.cao.awa.kora.entry.SampleEntrypoint#entry"
}
```

And writes code like this:

```kotlin
package com.github.cao.awa.kora.entry

import com.github.cao.awa.kora.config.KoraLaunchConfig
import com.github.cao.awa.kora.server.network.http.KoraHttpServer
import com.github.cao.awa.kora.server.network.http.builder.http
import com.github.cao.awa.kora.server.network.http.exception.path.HttpPathNotRegisteredException

object SampleEntrypoint {
    @JvmStatic
    fun entry(config: KoraLaunchConfig) {
        val httpConfig = config.httpServerConfig

        val http = http {
            // Setup static asset path.
            assets(config.assetPath)

            // Redirect all no registered query to 404 page.
            ifAbort(HttpPathNotRegisteredException::class) {
                withAsset(redirectAsset = config.errorPage)
            }
        }

        KoraHttpServer(http).start(
            port = config.serverPort,
            address = config.serverHost,
            useEpoll = httpConfig.useEpoll(),
            config = httpConfig
        )
    }
}
```

> NOTICE:\
> If you are using the libraries that aren't Kora integrated,\
> Then you must put the library build jar into the '{working_path}/libs/' path.

And build a  jar, and put it into ``'{working_path}/libs/ `` path, don't use shadow jar, shadow jar will pack useless classes that already provided by Kora jar.

The ``entrypoint`` config must declare full package name and class name, use symbol '#' to split entrypoint method.

Kora bootstrap must receive a  ``KoraLaunchConfig`` instance or ``Array<String>`` argument, and must annotated by ``@JvmStatic``, it also must is an kotlin object instead of kotlin class.

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