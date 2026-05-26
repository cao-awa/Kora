package com.github.cao.awa.kora.server.network.http.entrypoint

import com.github.cao.awa.kora.launch.config.KoraLaunchConfig
import com.github.cao.awa.kora.server.network.http.KoraHttpServer
import com.github.cao.awa.kora.server.network.http.builder.http
import com.github.cao.awa.kora.server.network.http.config.KoraHttpServerConfig
import com.github.cao.awa.kora.server.network.http.exception.path.HttpPathNotRegisteredException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File

object KoraHttpServerEntrypoint {
    private val LOGGER: Logger = LogManager.getLogger("KoraHttpServerEntrypoint")

    @JvmStatic
    fun entry(config: KoraLaunchConfig) {
        val serverConfig = KoraHttpServerConfig.create(File("configs/kora_http.json"))

        val nettyConfig = serverConfig.nettyServerConfig()
        val assetManagerConfig = serverConfig.assetManagerConfig()

        if (config.printConfigDetails()) {
            // Asset manager configs
            LOGGER.info("-- Asset manager configs --")
            LOGGER.info("Config 'enable': {}", assetManagerConfig.enable())
            LOGGER.info("Config 'asset_path': {}", assetManagerConfig.assetPath())
            LOGGER.info("Config 'error_page': {}", assetManagerConfig.errorPage())

            // Netty configs.
            LOGGER.info("-- Netty configs --")
            LOGGER.info("Config 'io': {}", nettyConfig.ioName())
            LOGGER.info("Config 'backlog': {}", nettyConfig.backlog())
            LOGGER.info("Config 'keep_alive': {}", nettyConfig.keepalive())
            LOGGER.info("Config 'rcv_buffer': {}", nettyConfig.rcvBuf())
            LOGGER.info("Config 'reuse_address': {}", nettyConfig.reuseAddr())
            LOGGER.info("Config 'allocator': {}", nettyConfig.allocatorName())
            LOGGER.info("Config 'tcp_no_delay': {}", nettyConfig.tcpNoDelay())
        }

        val http = http {
            // Setup static asset path.
            assets(assetManagerConfig.assetPath())

            // Redirect all no registered query to 404 page.
            ifAbort(HttpPathNotRegisteredException::class) {
                withAsset(redirectAsset = assetManagerConfig.errorPage())
            }
        }

        KoraHttpServer(http).start(
            port = serverConfig.serverPort(),
            address = serverConfig.serverHost(),
            io = nettyConfig.io(),
            httpServerConfig = serverConfig
        )
    }
}