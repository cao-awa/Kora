package com.github.cao.awa.kora.server.network.http.asset.config

object KoraAssetManagerDefaultConfig: KoraAssetManagerConfig() {
    private fun throwWhenSet() {
        error("Cannot set config in default server config instance")
    }

    override fun assetPath(assetPath: String): KoraAssetManagerConfig {
        throwWhenSet()
        return this
    }

    override fun errorPage(errorPage: String): KoraAssetManagerConfig {
        throwWhenSet()
        return this
    }
}