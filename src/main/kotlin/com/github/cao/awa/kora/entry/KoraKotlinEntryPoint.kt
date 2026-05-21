package com.github.cao.awa.kora.entry

import com.github.cao.awa.kora.config.KoraLaunchConfig
import com.github.cao.awa.kora.constant.KoraInformation
import com.github.cao.awa.kora.server.network.http.KoraHttpServer
import com.github.cao.awa.kora.server.network.http.builder.http
import com.github.cao.awa.kora.server.network.http.exception.path.HttpPathNotRegisteredException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.lang.reflect.Method

object KoraKotlinEntryPoint {
    private val LOGGER: Logger = LogManager.getLogger("KoraEntryPoint")

    @JvmStatic
    fun entry(config:  KoraLaunchConfig) {
            val httpConfig = config.httpServerConfig

            val http = http {
                // Setup static asset path.
                assets(config.assetPath)

                // Redirect all no registered query to 404 page.
                ifAbort(HttpPathNotRegisteredException::class) {
                    withAsset(redirectAsset = config.errorPage)
                }
            }

            KoraHttpServer(http).start(
                port = config.serverPort,
                address = config.serverHost,
                useEpoll = httpConfig.useEpoll(),
                config = httpConfig
            )
            config.entrypoint.also { entryPoint ->
                println(entryPoint.substring(0, entryPoint.indexOf("#")))
                Class.forName(entryPoint.substring(0, entryPoint.indexOf("#"))).methods.forEach {

                }
        }
    }

    @JvmStatic
    fun printConfigs(config: KoraLaunchConfig) {
        val httpConfig = config.httpServerConfig
        if (config.printConfigDetails) {
            LOGGER.info("Config 'print_config_details': {}", config.printConfigDetails)
            LOGGER.info("Config 'server_port': {}", config.serverPort)
            LOGGER.info("Config 'server_host': {}", config.serverHost)
            LOGGER.info("Config 'asset_path': {}", config.assetPath)
            LOGGER.info("Config 'error_page': {}", config.errorPage)
            LOGGER.info("Config 'entrypoint': {}", config.entrypoint)

            LOGGER.info("Config 'use_epoll': {}", httpConfig.useEpoll())
            LOGGER.info("Config 'backlog': {}", httpConfig.backlog())
            LOGGER.info("Config 'keep_alive': {}", httpConfig.keepalive())
            LOGGER.info("Config 'rcv_buffer': {}", httpConfig.rcvBuf())
            LOGGER.info("Config 'reuse_address': {}", httpConfig.reuseAddr())
            LOGGER.info("Config 'tcp_no_delay': {}", httpConfig.tcpNoDelay())
        }
    }

    @JvmStatic
    fun entryPointNotFount(name: String):  Nothing {
        throw IllegalArgumentException("Entrypoint $name not found, please ensure it present and arguments be right")
    }

    @JvmStatic
    fun entryToDeclared(
        config: KoraLaunchConfig,
        args: Array<String>
    ) {
        val entryPoint: String = config.entrypoint
        if (!entryPoint.contains("#")){
            throw IllegalArgumentException("Entrypoint doesn't contain a method declare")
        }

        LOGGER.info("Launching Kora server({}) with declared entrypoint '{}'", KoraInformation.VERSION, entryPoint)

        val classLoader = Thread.currentThread().contextClassLoader
        val entryClassName = entryPoint.substring(0, entryPoint.indexOf("#"))
        val entryMethodName = entryPoint.substring(entryPoint.indexOf("#") + 1)
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
                method = entryClass.getMethod(
                    entryMethodName,
                    Array<String>::class.java
                )

                method.invoke(null, args)
            }
        } catch (_: ClassNotFoundException) {
            entryPointNotFount(entryPoint)
        } catch (_: NoSuchMethodException) {
            throw IllegalArgumentException("Cannot found valid entrypoint, please ensure method '$entryMethodName' received a 'KoraLaunchConfig' or 'Array<String>' and be static and @JvmStatic annotated")
        }
    }
}