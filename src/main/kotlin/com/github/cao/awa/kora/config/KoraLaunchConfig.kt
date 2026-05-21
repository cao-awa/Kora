package com.github.cao.awa.kora.config

import com.github.cao.awa.cason.obj.JSONObject
import com.github.cao.awa.cason.serialize.parser.JSONParser
import com.github.cao.awa.kora.server.network.config.KoraNettyServerConfig
import com.github.cao.awa.kora.server.network.config.KoraNettyServerDefaultConfig
import com.github.cao.awa.kora.server.network.http.asset.config.KoraAssetManagerConfig
import com.github.cao.awa.kora.server.network.http.asset.config.KoraAssetManagerDefaultConfig
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File

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
                        config.entrypoint = entrypoint
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

    var printConfigDetails: Boolean = true
    var serverPort: Int = 12345
    var serverHost: String = "localhost"
    var assetManagerConfig: KoraAssetManagerConfig = KoraAssetManagerDefaultConfig
    var nettyServerConfig: KoraNettyServerConfig<*> = KoraNettyServerDefaultConfig
    var entrypoint: String = "com.github.cao.awa.kora.entry.KoraKotlinEntryPoint#entry"

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

    fun entrypoint(): String {
        return this.entrypoint
    }

    open fun entrypoint(entrypoint: String): KoraLaunchConfig {
        this.entrypoint = entrypoint
        return this
    }

    fun isDefaultEntrypoint(): Boolean {
        return this.entrypoint.isEmpty() || this.entrypoint == "com.github.cao.awa.kora.entry.KoraKotlinEntryPoint#entry"
    }

    fun toJSON(): JSONObject {
        return JSONObject {
            "print_config_details" set printConfigDetails
            "server_port" set serverPort
            "server_host" set serverHost
            "asset_manager" set assetManagerConfig.toJSON()
            "netty" set nettyServerConfig.toJSON()
            "entrypoint" set entrypoint
        }
    }
}