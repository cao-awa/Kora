package com.github.cao.awa.kora.config

import com.github.cao.awa.cason.obj.JSONObject
import com.github.cao.awa.cason.primary.JSONString
import com.github.cao.awa.cason.serialize.parser.JSONParser
import com.github.cao.awa.kora.kt.extent.onlyContains
import com.github.cao.awa.kora.server.network.config.KoraNettyServerConfig
import com.github.cao.awa.kora.server.network.config.KoraNettyServerDefaultConfig
import com.github.cao.awa.kora.server.network.http.asset.config.KoraAssetManagerConfig
import com.github.cao.awa.kora.server.network.http.asset.config.KoraAssetManagerDefaultConfig
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.util.LinkedList

open class KoraLaunchConfig {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger("KoraLaunchConfig")

        @JvmStatic
        fun createConfig(file: File): KoraLaunchConfig {
            val config = KoraLaunchConfig()
            if (file.isFile) {
                LOGGER.info("Creating config from file '{}'", file.absolutePath)
                JSONParser.parseObject(file.readText(Charsets.UTF_8)).let {
                    it.getBoolean("print_config_details")?.let { printConfigDetails ->
                        config.printConfigDetails = printConfigDetails
                    }
                    it.getInt("server_port")?.let { serverPort ->
                        config.serverPort = serverPort
                    }
                    it.getString("server_host")?.let { serverHost ->
                        config.serverHost = serverHost
                    }
                    it.getJSON("asset_manager")?.let { assetManager ->
                        config.assetManagerConfig = KoraAssetManagerConfig.createFromJSON(assetManager)
                    }
                    it.getJSON("netty")?.let { http ->
                        config.nettyServerConfig = KoraNettyServerConfig.createFromJSON(http)
                    }
                    it.getString("entrypoint")?.let { entrypoint ->
                        config.entrypoint.clear()
                        config.entrypoint.add(entrypoint)
                    } ?: it.getArray("entrypoint")?.let { entrypoints ->
                        config.entrypoint.clear()
                        entrypoints.forEach { entrypoint ->
                            if (!entrypoint.isString()) {
                                throw IllegalArgumentException("Entrypoint definition must be string")
                            }
                            if (entrypoint is JSONString) {
                                config.entrypoint.add(entrypoint.asString())
                            }
                        }
                    }
                }
            } else {
                LOGGER.info("Config not found, creating config to file '{}'", file.absolutePath)
                file.parentFile.let {
                    if (!it.exists()) {
                        it.mkdirs()
                    }
                }
            }
            file.writeText(config.toJSON().toString(true, "    "))
            return config
        }
    }

    private var printConfigDetails: Boolean = true
    private var serverPort: Int = 12345
    private var serverHost: String = "localhost"
    private var assetManagerConfig: KoraAssetManagerConfig = KoraAssetManagerDefaultConfig
    private var nettyServerConfig: KoraNettyServerConfig<*> = KoraNettyServerDefaultConfig
    private var entrypoint: LinkedHashSet<String> =
        linkedSetOf("com.github.cao.awa.kora.entrypoint.KoraKotlinEntrypoint#entry")

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

    fun entrypoint(): LinkedHashSet<String> {
        return this.entrypoint
    }

    open fun entrypoint(entrypoint: LinkedHashSet<String>): KoraLaunchConfig {
        this.entrypoint = entrypoint
        return this
    }

    fun isDefaultEntrypoint(): Boolean {
        return this.entrypoint.isEmpty() || this.entrypoint.onlyContains("com.github.cao.awa.kora.entry.KoraKotlinEntryPoint#entry")
    }

    fun toJSON(): JSONObject {
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