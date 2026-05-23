package com.github.cao.awa.kora.entrypoint

import com.github.cao.awa.kora.KoraEntrypoint
import com.github.cao.awa.kora.entrypoint.exception.KoraEntrypointStageFailedException
import com.github.cao.awa.kora.launch.config.KoraLaunchConfig
import com.github.cao.awa.kora.entrypoint.lib.KoraLibraryLoader
import com.github.cao.awa.kora.server.network.http.KoraHttpServer
import com.github.cao.awa.kora.server.network.http.builder.http
import com.github.cao.awa.kora.server.network.http.exception.path.HttpPathNotRegisteredException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.lang.reflect.InvocationTargetException
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
            if (config.entrypoint().size == 1) {
                LOGGER.info("Config 'entrypoint': {}", config.entrypoint().first)
            } else {
                LOGGER.info("Config 'entrypoint': ")
                for (entrypoint in config.entrypoint()) {
                    LOGGER.info(" + '$entrypoint'")
                }
            }

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
        inputEntrypoint: String
    ) {
        var entrypoint: String
        val plugin = KoraEntrypoint.DEPENDENCIES_MANAGER.getPlugin(inputEntrypoint)
        if (!inputEntrypoint.contains("#")) {
            if (plugin == null) {
                throw KoraEntrypointStageFailedException(
                    inputEntrypoint,
                    IllegalArgumentException("Entrypoint '$inputEntrypoint' doesn't contain a method declare correctly, it not a full method definition or a present plugin name")
                )
            } else {
                entrypoint = plugin.entrypoint
            }
        } else {
            entrypoint = inputEntrypoint
        }

        if (entrypoint == "") {
            throw KoraEntrypointStageFailedException(
                inputEntrypoint,
                IllegalArgumentException("Entrypoint doesn't contain a method declare")
            )
        }

        if (plugin != null) {
            for (dependsOn in plugin.dependsOn) {
                if (!KoraEntrypoint.DEPENDENCIES_MANAGER.isPluginLoaded(dependsOn)) {
                    throw KoraEntrypointStageFailedException(
                        inputEntrypoint,
                        IllegalStateException("Plugin '${plugin.name}' depends on plugin' $dependsOn' but it doesn't be loaded, please load it first ")
                    )
                }
            }
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

            if (plugin != null) {
                KoraEntrypoint.DEPENDENCIES_MANAGER.onPluginLoad(plugin.name)
            }
        } catch (_: ClassNotFoundException) {
            entryPointNotFount(entrypoint)
        } catch (_: NoSuchMethodException) {
            throw IllegalArgumentException("Cannot found valid entrypoint, please ensure method '$entryMethodName' received a 'KoraLaunchConfig' or 'Array<String>' or empty parameter and be static and @JvmStatic annotated")
        } catch (invocationException: InvocationTargetException) {
            val throwError = invocationException.cause ?: invocationException
            throw KoraEntrypointStageFailedException(entrypoint, throwError)
        }
    }
}