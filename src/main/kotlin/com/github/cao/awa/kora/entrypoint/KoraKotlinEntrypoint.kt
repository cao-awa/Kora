package com.github.cao.awa.kora.entrypoint

import com.github.cao.awa.kora.config.KoraLaunchConfig
import com.github.cao.awa.kora.constant.KoraInformation
import com.github.cao.awa.kora.entrypoint.lib.KoraLibraryLoader
import com.github.cao.awa.kora.server.network.http.KoraHttpServer
import com.github.cao.awa.kora.server.network.http.builder.http
import com.github.cao.awa.kora.server.network.http.exception.path.HttpPathNotRegisteredException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.lang.reflect.Method
import kotlin.jvm.Throws

object KoraKotlinEntrypoint {
    private val LOGGER: Logger = LogManager.getLogger("KoraEntryPoint")

    @JvmStatic
    fun entry(config: KoraLaunchConfig) {
        val nettyConfig = config.nettyServerConfig()

        val assetManagerConfig = config.assetManagerConfig()

        val http = http {
            // Setup static asset path.
            assets(assetManagerConfig.assetPath())

            // Redirect all no registered query to 404 page.
            ifAbort(HttpPathNotRegisteredException::class) {
                withAsset(redirectAsset = assetManagerConfig.errorPage())
            }
        }

        KoraHttpServer(http).start(
            port = config.serverPort(),
            address = config.serverHost(),
            io = nettyConfig.io(),
            launchConfig = config
        )
    }

    @JvmStatic
    fun printConfigs(config: KoraLaunchConfig) {
        val nettyConfig = config.nettyServerConfig()
        val assetManagerConfig = config.assetManagerConfig()
        // Print configs if 'printConfigDetails' is enabled
        if (config.printConfigDetails()) {
            // Root configs.
            LOGGER.info("-- Basic configs --")
            LOGGER.info("Config 'print_config_details': {}", config.printConfigDetails())
            LOGGER.info("Config 'server_port': {}", config.serverPort())
            LOGGER.info("Config 'server_host': {}", config.serverHost())
            LOGGER.info("Config 'entrypoint': {}", config.entrypoint())

            // Asset manager configs
            LOGGER.info("-- Asset manager configs --")
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
    }

    @JvmStatic
    @Throws(IllegalArgumentException::class)
    fun entryPointNotFount(name: String): Nothing {
        throw IllegalArgumentException("Entrypoint $name not found, please ensure it present and arguments be right")
    }

    @JvmStatic
    fun entryToDeclared(
        config: KoraLaunchConfig,
        args: Array<String>,
    ) {
        for (entrypoint in config.entrypoint()) {
            entryToDeclared(
                config,
                args,
                entrypoint
            )
        }
    }

    fun entryToDeclared(
        config: KoraLaunchConfig,
        args: Array<String>,
        entrypoint: String
    ) {
        if (!entrypoint.contains("#")) {
            throw IllegalArgumentException("Entrypoint doesn't contain a method declare")
        }

        LOGGER.info("Launching declared entrypoint '{}'", entrypoint)

        val classLoader = KoraLibraryLoader.classLoader!!
        val entryClassName = entrypoint.substring(0, entrypoint.indexOf("#"))
        val entryMethodName = entrypoint.substring(entrypoint.indexOf("#") + 1)
        try {
            val entryClass = classLoader.loadClass(entryClassName)
            var method: Method?
            try {
                method = entryClass.getMethod(
                    entryMethodName,
                    KoraLaunchConfig::class.java
                )

                method.invoke(null, config)
            } catch (_: NoSuchMethodException) {
                try {
                    method = entryClass.getMethod(
                        entryMethodName,
                        Array<String>::class.java
                    )

                    method.invoke(null, args)
                } catch (_: NoSuchMethodException) {
                    method = entryClass.getMethod(
                        entryMethodName
                    )

                    method.invoke(null)
                }
            }
        } catch (_: ClassNotFoundException) {
            entryPointNotFount(entrypoint)
        } catch (_: NoSuchMethodException) {
            throw IllegalArgumentException("Cannot found valid entrypoint, please ensure method '$entryMethodName' received a 'KoraLaunchConfig' or 'Array<String>' or empty parameter and be static and @JvmStatic annotated")
        }
    }
}