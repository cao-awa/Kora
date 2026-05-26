package com.github.cao.awa.kora.server.network.http.config

import com.github.cao.awa.kora.server.network.http.asset.config.KoraAssetManagerConfig

object KoraHttpDefaultServerConfig: KoraHttpServerConfig() {
    private fun throwWhenSet(): Nothing {
        error("Cannot set config in default server config instance")
    }

    override fun serverPort(port: Int): KoraHttpServerConfig {
        throwWhenSet()
    }

    override fun serverHost(host: String): KoraHttpServerConfig {
        throwWhenSet()
    }

    override fun assetManagerConfig(config: KoraAssetManagerConfig): KoraHttpServerConfig {
        throwWhenSet()
    }
}