package com.github.cao.awa.kora.launch.config

import com.github.cao.awa.cason.obj.JSONObject
import com.github.cao.awa.cason.primary.JSONString
import com.github.cao.awa.kora.config.KoraConfig
import com.github.cao.awa.kora.kt.extent.onlyContains
import com.github.cao.awa.kora.server.network.config.KoraNettyServerConfig
import com.github.cao.awa.kora.server.network.config.KoraNettyServerDefaultConfig
import com.github.cao.awa.kora.server.network.http.asset.config.KoraAssetManagerConfig
import com.github.cao.awa.kora.server.network.http.asset.config.KoraAssetManagerDefaultConfig
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.util.LinkedList

open class KoraLaunchConfig: KoraConfig() {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger("KoraLaunchConfig")

        @JvmStatic
        fun createConfig(file: File): KoraLaunchConfig {
            return createConfig(file)  {
                val config = KoraLaunchConfig()
                ifBoolean("print_config_details") {
                    config.printConfigDetails = this
                }
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
                ifString("entrypoint") {
                    if (this != "") {
                        config.entrypoint.clear()
                        config.entrypoint.add(this)
                    }
                }
                ifArray("entrypoint") {
                    config.entrypoint.clear()
                    forEach { entrypoint ->
                        if (!entrypoint.isString()) {
                            throw IllegalArgumentException("Entrypoint definition must be string")
                        }
                        if (entrypoint is JSONString) {
                            config.entrypoint.add(entrypoint.asString())
                        }
                    }
                }

                config
            }
        }
    }

    private var printConfigDetails: Boolean = true
    private var serverPort: Int = 12345
    private var serverHost: String = "0.0.0.0"
    private var assetManagerConfig: KoraAssetManagerConfig = KoraAssetManagerDefaultConfig
    private var nettyServerConfig: KoraNettyServerConfig<*> = KoraNettyServerDefaultConfig
    private var entrypoint: LinkedList<String> =
        LinkedList<String>().also {
            it.add("com.github.cao.awa.kora.entrypoint.KoraKotlinEntrypoint#entry")
        }
    private var error: Throwable? = null
    private var sharedContext: MutableMap<String, String> = mutableMapOf()

    fun printConfigDetails(): Boolean {
        return this.printConfigDetails
    }

    open fun printConfigDetails(print: Boolean): KoraLaunchConfig {
        this.printConfigDetails = print
        return this
    }

    fun serverPort(): Int {
        return this.serverPort
    }

    open fun serverPort(port: Int): KoraLaunchConfig {
        this.serverPort = port
        return this
    }

    fun serverHost(): String {
        return this.serverHost
    }

    open fun serverHost(host: String): KoraLaunchConfig {
        this.serverHost = host
        return this
    }

    fun assetManagerConfig(): KoraAssetManagerConfig {
        return this.assetManagerConfig
    }

    open fun assetManagerConfig(config: KoraAssetManagerConfig): KoraLaunchConfig {
        this.assetManagerConfig = config
        return this
    }

    fun nettyServerConfig(): KoraNettyServerConfig<*> {
        return this.nettyServerConfig
    }

    open fun nettyServerConfig(config: KoraNettyServerConfig<*>): KoraLaunchConfig {
        this.nettyServerConfig = config
        return this
    }

    fun entrypoint(): LinkedList<String> {
        return this.entrypoint
    }

    open fun entrypoint(entrypoint: LinkedList<String>): KoraLaunchConfig {
        this.entrypoint = entrypoint
        return this
    }

    fun error(): Throwable {
        return this.error!!
    }

    open fun error(error: Throwable): KoraLaunchConfig {
        this.error = error
        return this
    }

    fun resetError(): KoraLaunchConfig {
        this.error = null
        return this
    }

    operator fun set(key: String, data: String) {
        this.sharedContext[key] = data
    }

    operator fun get(key: String): String? {
        return this.sharedContext[key]
    }

    fun isDefaultEntrypoint(): Boolean {
        return this.entrypoint.isEmpty() || this.entrypoint.onlyContains("com.github.cao.awa.kora.entry.KoraKotlinEntryPoint#entry")
    }

    override fun toJSON(): JSONObject {
        return JSONObject {
            "print_config_details" set printConfigDetails
            "server_port" set serverPort
            "server_host" set serverHost
            "asset_manager" set assetManagerConfig.toJSON()
            "netty" set nettyServerConfig.toJSON()
            arr("entrypoint") {
                for (entry in entrypoint) {
                    +entry
                }
            }
        }
    }
}