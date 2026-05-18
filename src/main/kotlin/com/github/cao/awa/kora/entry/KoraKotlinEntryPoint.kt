package com.github.cao.awa.kora.entry

import com.github.cao.awa.kora.config.KoraLaunchConfig
import com.github.cao.awa.kora.server.network.http.KoraHttpServer
import com.github.cao.awa.kora.server.network.http.builder.http
import com.github.cao.awa.kora.server.network.http.exception.path.HttpPathNotRegisteredException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File

object KoraKotlinEntryPoint {
    private val LOGGER: Logger = LogManager.getLogger("KoraEntryPoint")

    @JvmStatic
    fun main(args: Array<String>) {
        val config = KoraLaunchConfig.readConfig(File(("config/launch.json")))

        val http = http {
            // Setup static asset path.
            assets(config.assetPath)

            // Redirect all no registered query to 404 page.
            ifAbort(HttpPathNotRegisteredException::class) {
                withAsset(redirectAsset = config.errorPage)
            }
        }

        LOGGER.info("Starting Kora server...")
        LOGGER.info("Config 'server_port': {}", config.serverPort)
        LOGGER.info("Config 'server_host': {}", config.serverHost)
        LOGGER.info("Config 'asset_path': {}", config.assetPath)
        LOGGER.info("Config 'error_page': {}", config.errorPage)

        val httpConfig = config.httpServerConfig
        LOGGER.info("Config 'use_epoll': {}", httpConfig.useEpoll())
        LOGGER.info("Config 'backlog': {}", httpConfig.backlog())
        LOGGER.info("Config 'keep_alive': {}", httpConfig.keepalive())
        LOGGER.info("Config 'rcv_buffer': {}", httpConfig.rcvBuf())
        LOGGER.info("Config 'reuse_address': {}", httpConfig.reuseAddr())
        LOGGER.info("Config 'tcp_no_delay': {}", httpConfig.tcpNoDelay())

        KoraHttpServer(http).start(
            port = config.serverPort,
            address = config.serverHost,
            useEpoll = httpConfig.useEpoll(),
            config = httpConfig
        )
    }
}