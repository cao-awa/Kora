package com.github.cao.awa.kora.config

import com.github.cao.awa.cason.serialize.parser.JSONParser
import com.github.cao.awa.kora.server.network.http.config.KoraHttpDefaultServerConfig
import com.github.cao.awa.kora.server.network.http.config.KoraHttpServerConfig
import java.io.File

class KoraLaunchConfig {
    companion object {
        fun readConfig(file: File): KoraLaunchConfig {
            val config = KoraLaunchConfig()
            if (file.exists()) {
                JSONParser.parseObject(file.readText(Charsets.UTF_8)).let {
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
                    it.getBoolean("use_epoll")?.let { useEpoll ->
                        config.useEpoll = useEpoll
                    }
                    it.getJSON("http")?.let { http ->
                        config.httpServerConfig = KoraHttpServerConfig.createFromJSON(http)
                    }
                }
            }
            return config
        }
    }

    var serverPort: Int = 12345
    var serverHost: String = "localhost"
    var assetPath: String = ""
    var errorPage: String = ""
    var useEpoll: Boolean = true
    var httpServerConfig: KoraHttpServerConfig = KoraHttpDefaultServerConfig
}