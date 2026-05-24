package com.github.cao.awa.kora.launch.config

import com.github.cao.awa.kora.server.network.config.KoraNettyServerConfig
import com.github.cao.awa.kora.server.network.http.asset.config.KoraAssetManagerConfig
import java.util.LinkedList

object KoraLaunchDefaultConfig: KoraLaunchConfig() {
    private fun throwWhenSet(): Nothing {
        error("Cannot set config in default server config instance")
    }

    override fun printConfigDetails(print: Boolean): KoraLaunchConfig {
        throwWhenSet()
    }

    override fun serverPort(port: Int): KoraLaunchConfig {
        throwWhenSet()
    }

    override fun serverHost(host: String): KoraLaunchConfig {
        throwWhenSet()
    }

    override fun assetManagerConfig(config: KoraAssetManagerConfig): KoraLaunchConfig {
        throwWhenSet()
    }

    override fun nettyServerConfig(config: KoraNettyServerConfig<*>): KoraLaunchConfig {
        throwWhenSet()
    }

    override fun entrypoint(entrypoint: LinkedList<String>): KoraLaunchConfig {
        throwWhenSet()
    }

    override fun error(error: Throwable): KoraLaunchConfig {
        throwWhenSet()
    }
}