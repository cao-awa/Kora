package com.github.cao.awa.kora.config

import com.github.cao.awa.cason.obj.JSONObject
import com.github.cao.awa.cason.serialize.parser.JSONParser
import com.github.cao.awa.kora.server.network.http.config.KoraHttpDefaultServerConfig
import com.github.cao.awa.kora.server.network.http.config.KoraHttpServerConfig
import java.io.File

open class KoraLaunchConfig {
    companion object {
        @JvmStatic
        fun createConfig(file: File): KoraLaunchConfig {
            val config = KoraLaunchConfig()
            if (file.isFile) {
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
                    it.getString("asset_path")?.let { assetPath ->
                        config.assetPath = assetPath
                    }
                    it.getString("error_page")?.let { errorPage ->
                        config.errorPage = errorPage
                    }
                    it.getJSON("http")?.let { http ->
                        config.httpServerConfig = KoraHttpServerConfig.createFromJSON(http)
                    }
                    it.getString("entrypoint")?.let { entrypoint ->
                        config.entrypoint = entrypoint
                    }
                }
            } else {
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
    var assetPath: String = "assets/"
    var errorPage: String = ""
    var httpServerConfig: KoraHttpServerConfig = KoraHttpDefaultServerConfig
    var entrypoint: String = ""

    fun isDefaultEntrypoint(): Boolean {
        return this.entrypoint.isEmpty() || this.entrypoint == "com.github.cao.awa.kora.entry.KoraKotlinEntryPoint#entry"
    }

    fun toJSON(): JSONObject {
        return JSONObject {
            "print_config_details" set printConfigDetails
            "server_port" set serverPort
            "server_host" set serverHost
            "asset_path" set assetPath
            "error_page" set errorPage
            "netty" set httpServerConfig.toJSON()
            "entrypoint" set entrypoint
        }
    }
}