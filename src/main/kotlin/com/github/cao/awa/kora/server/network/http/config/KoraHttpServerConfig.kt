package com.github.cao.awa.kora.server.network.http.config

import com.github.cao.awa.kora.server.network.config.KoraNettyConfig

open class KoraHttpServerConfig: KoraNettyConfig<KoraHttpServerConfig>() {
    private var tcpNoDelay: Boolean = true

    fun tcpNoDelay(): Boolean = this.tcpNoDelay

    open fun tcpNoDelay(tcpNoDelay: Boolean): KoraHttpServerConfig {
        this.tcpNoDelay = tcpNoDelay
        return this
    }

    override fun copy(): KoraHttpServerConfig {
        return KoraHttpServerConfig().also {
            super.copy(it)
        }
    }
}