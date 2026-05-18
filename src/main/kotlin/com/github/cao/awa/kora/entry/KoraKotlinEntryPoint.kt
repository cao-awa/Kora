package com.github.cao.awa.kora.entry

import com.github.cao.awa.kora.config.KoraLaunchConfig
import com.github.cao.awa.kora.server.network.http.KoraHttpServer
import com.github.cao.awa.kora.server.network.http.builder.http
import com.github.cao.awa.kora.server.network.http.exception.path.HttpPathNotRegisteredException
import java.io.File

object KoraKotlinEntryPoint {
    @JvmStatic
    fun main(args: Array<String>) {
        val config = KoraLaunchConfig.readConfig(File(("config/launch.json")))

        val http = http {
            // Setup static asset path.
            assets("assets/")

            // Redirect all no registered query to 404 page.
            ifAbort(HttpPathNotRegisteredException::class) {
                withAsset(redirectAsset = "error/404.html")
            }
        }

        KoraHttpServer(http).start(
            port = config.serverPort,
            address = config.serverHost,
            useEpoll = config.useEpoll,
            config = config.httpServerConfig
        )
    }
}