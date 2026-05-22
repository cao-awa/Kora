package com.github.cao.awa.kora.config

import com.github.cao.awa.kora.server.network.config.KoraNettyServerConfig
import com.github.cao.awa.kora.server.network.http.asset.config.KoraAssetManagerConfig

object KoraLaunchDefaultConfig: KoraLaunchConfig() {
    private fun throwWhenSet() {
        error("Cannot set config in default server config instance")
    }


    override fun printConfigDetails(print: Boolean): KoraLaunchConfig {
        throwWhenSet()
        return this
    }

    override fun serverPort(port: Int): KoraLaunchConfig {
        throwWhenSet()
        return this
    }

    override fun serverHost(host: String): KoraLaunchConfig {
        throwWhenSet()
        return this
    }

    override fun assetManagerConfig(config: KoraAssetManagerConfig): KoraLaunchConfig {
        throwWhenSet()
        return this
    }

    override fun nettyServerConfig(config: KoraNettyServerConfig<*>): KoraLaunchConfig {
        throwWhenSet()
        return this
    }

    override fun entrypoint(entrypoint: LinkedHashSet<String>): KoraLaunchConfig {
        throwWhenSet()
        return this
    }
}