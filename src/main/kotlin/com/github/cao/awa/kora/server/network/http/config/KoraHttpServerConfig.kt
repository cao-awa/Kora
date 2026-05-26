package com.github.cao.awa.kora.server.network.http.config

import com.github.cao.awa.cason.obj.JSONObject
import com.github.cao.awa.kora.config.KoraConfig
import com.github.cao.awa.kora.server.network.config.KoraNettyServerConfig
import com.github.cao.awa.kora.server.network.config.KoraNettyServerDefaultConfig
import com.github.cao.awa.kora.server.network.http.asset.config.KoraAssetManagerConfig
import com.github.cao.awa.kora.server.network.http.asset.config.KoraAssetManagerDefaultConfig
import java.io.File

open class KoraHttpServerConfig: KoraConfig() {
    companion object {
        @JvmStatic
        fun create(file: File): KoraHttpServerConfig {
            return createConfig(file) {
                val config = KoraHttpServerConfig()
                ifInt("server_port") {
                    config.serverPort = this
                }
                ifString("server_host") {
                    config.serverHost = this
                }
                ifJSON("asset_manager") {
                    config.assetManagerConfig = KoraAssetManagerConfig.createFromJSON(this)
                }
                ifJSON("netty") {
                    config.nettyServerConfig = KoraNettyServerConfig.createFromJSON(this)
                }
                config
            }
        }
    }

    private var serverPort: Int = 12345
    private var serverHost: String = "0.0.0.0"
    private var assetManagerConfig: KoraAssetManagerConfig = KoraAssetManagerDefaultConfig
    private var nettyServerConfig: KoraNettyServerConfig = KoraNettyServerDefaultConfig

    fun serverPort(): Int {
        return this.serverPort
    }

    open fun serverPort(port: Int): KoraHttpServerConfig {
        this.serverPort = port
        return this
    }

    fun serverHost(): String {
        return this.serverHost
    }

    open fun serverHost(host: String): KoraHttpServerConfig {
        this.serverHost = host
        return this
    }

    fun assetManagerConfig(): KoraAssetManagerConfig {
        return this.assetManagerConfig
    }

    open fun assetManagerConfig(config: KoraAssetManagerConfig): KoraHttpServerConfig {
        this.assetManagerConfig = config
        return this
    }

    fun nettyServerConfig(): KoraNettyServerConfig {
        return this.nettyServerConfig
    }

    open fun nettyServerConfig(config: KoraNettyServerConfig): KoraHttpServerConfig {
        this.nettyServerConfig = config
        return this
    }

    override fun toJSON(): JSONObject {
        return JSONObject {
            "server_port" set serverPort
            "server_host" set serverHost
            "asset_manager" set assetManagerConfig.toJSON()
            "netty" set nettyServerConfig.toJSON()
        }
    }
}