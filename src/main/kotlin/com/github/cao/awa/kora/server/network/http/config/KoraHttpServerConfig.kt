package com.github.cao.awa.kora.server.network.http.config

import com.github.cao.awa.cason.obj.JSONObject
import com.github.cao.awa.kora.server.network.config.KoraNettyConfig

open class KoraHttpServerConfig: KoraNettyConfig<KoraHttpServerConfig>() {
    companion object {
        fun createFromJSON(json: JSONObject): KoraHttpServerConfig {
            val config = KoraHttpServerConfig()
            json.getBoolean("use_epoll")?.let { useEpoll ->
                config.useEpoll(useEpoll)
            }
            json.getInt("backlog")?.let {
                config.backlog(it)
            }
            json.getBoolean("keep_alive")?.let {
                config.keepalive(it)
            }
            json.getInt("rcv_buffer")?.let {
                config.rcvBuf(it)
            }
            json.getBoolean("reuse_address")?.let {
                config.reuseAddr(it)
            }
            json.getBoolean("tcp_no_delay")?.let {
                config.tcpNoDelay(it)
            }
            return config
        }
    }



    override fun copy(): KoraHttpServerConfig {
        return KoraHttpServerConfig().also {
            super.copy(it)
        }
    }
}