package com.github.cao.awa.kora.entrypoint

import com.github.cao.awa.kora.entrypoint.exception.KoraEntrypointStageFailedException
import com.github.cao.awa.kora.launch.config.KoraLaunchConfig
import com.github.cao.awa.kora.entrypoint.lib.KoraLibraryLoader
import com.github.cao.awa.kora.plugin.KoraPlugin
import com.github.cao.awa.kora.plugin.markEntrypointLoaded
import com.github.cao.awa.kora.plugin.markPluginLoaded
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Modifier
import kotlin.jvm.Throws

object KoraKotlinEntrypoint {
    private val LOGGER: Logger = LogManager.getLogger("KoraEntryPoint")

    @JvmStatic
    fun printConfigs(config: KoraLaunchConfig) {
        // Print configs if 'printConfigDetails' is enabled
        if (config.printConfigDetails()) {
            // Root configs.
            LOGGER.info("-- Basic configs --")
            LOGGER.info("Config 'print_config_details': {}", config.printConfigDetails())
            if (config.entrypoint().size == 1) {
                LOGGER.info("Config 'entrypoint': {}", config.entrypoint()[0])
            } else {
                LOGGER.info("Config 'entrypoint': ")
                for (entrypoint in config.entrypoint()) {
                    LOGGER.info(" + '$entrypoint'")
                }
            }
        }
    }

    @JvmStatic
    @Throws(IllegalArgumentException::class)
    fun entryPointNotFount(name: String): Nothing {
        throw IllegalArgumentException("Entrypoint '$name' not found, please ensure it present and arguments be right")
    }

    @JvmStatic
    fun entryToDeclared(
        config: KoraLaunchConfig,
        args: Array<String>
    ) {
        val repeatDetect = mutableListOf<String>()

        for (entrypoint in config.entrypoint()) {
            if (repeatDetect.contains(entrypoint)) {
                throw IllegalArgumentException("Entrypoint '$entrypoint' already defined, cannot repeat it again")
            }
            repeatDetect.add(entrypoint)
        }

        for (entrypoint in config.entrypoint()) {
            val plugin = KoraEntrypoint.DEPENDENCIES_MANAGER.getPlugin(entrypoint)

            try {
                entryToDeclared(
                    config,
                    args,
                    entrypoint,
                    plugin,
                    false
                )
            } catch (failedException: KoraEntrypointStageFailedException) {
                if (plugin != null && plugin.fallback != "") {
                    LOGGER.warn(
                        "Plugin '{}' entrypoint '{}' failed, now try fallback '{}'",
                        plugin.name,
                        plugin.entrypoint,
                        plugin.fallback,
                        failedException.cause
                    )
                    config.error(failedException.cause)
                    entryToDeclared(
                        config,
                        args,
                        plugin.fallback,
                        plugin,
                        true
                    )
                    config.resetError()
                } else {
                    throw failedException
                }
            }
        }
    }

    fun entryToDeclared(
        config: KoraLaunchConfig,
        args: Array<String>,
        inputEntrypoint: String,
        plugin: KoraPlugin?,
        isFallback: Boolean,
    ) {
        val entrypoint = if (!inputEntrypoint.contains("#")) {
            if (isFallback) {
                inputEntrypoint
            } else {
                plugin?.entrypoint ?: throw KoraEntrypointStageFailedException(
                    inputEntrypoint,
                    IllegalArgumentException("Entrypoint '$inputEntrypoint' doesn't contain a method declare correctly, it not a full method definition or a present plugin name")
                )
            }
        } else {
            inputEntrypoint
        }

        if (entrypoint == "") {
            throw KoraEntrypointStageFailedException(
                inputEntrypoint,
                IllegalArgumentException("Entrypoint doesn't contain a method declare")
            )
        }

        if (plugin != null) {
            for (dependsOn in plugin.dependsOn) {
                if (!isFallback && !KoraEntrypoint.DEPENDENCIES_MANAGER.isPluginLoaded(dependsOn)) {
                    throw KoraEntrypointStageFailedException(
                        inputEntrypoint,
                        IllegalStateException("Plugin '${plugin.name}' depends on plugin' $dependsOn' but it doesn't be loaded, please load it first ")
                    )
                }
            }
        }
        LOGGER.info("Launching declared entrypoint '{}'", entrypoint)

        val classLoader = KoraLibraryLoader.classLoader
        val entryClassName = getEntrypointClass(entrypoint)
        val entryMethodName = getEntrypointMethod(entrypoint)
        try {
            val entryClass = classLoader.loadClass(entryClassName)
            var executed = false
            for (method in entryClass.methods) {
                if (Modifier.isStatic(method.modifiers) && method.name == entryMethodName) {
                    if (method.parameterCount == 1) {
                        val parameterType = method.parameterTypes[0].kotlin
                        if (parameterType == KoraLaunchConfig::class) {
                            method(null, config)
                            executed = true
                            break
                        }
                        if (parameterType == Array<String>::class) {
                            method(null, args)
                            executed = true
                            break
                        }

                        if (isFallback && parameterType == Throwable::class) {
                            method(null, config.error())
                            executed = true
                            break
                        }
                    } else {
                        method(null)
                        executed = true
                        break
                    }
                }
            }

            if (!executed) {
                throw IllegalArgumentException("Cannot found valid entrypoint, please ensure method '$entryMethodName' received a 'KoraLaunchConfig' or 'Array<String>' or empty parameter and be static and @JvmStatic annotated")
            }

            if (plugin != null) {
                markPluginLoaded(plugin)
            }
            markEntrypointLoaded(entrypoint, plugin)
        } catch (_: ClassNotFoundException) {
            entryPointNotFount(entrypoint)
        } catch (invocationException: InvocationTargetException) {
            val throwError = invocationException.cause ?: invocationException
            throw KoraEntrypointStageFailedException(entrypoint, throwError)
        }
    }

    fun getEntrypointClass(entrypoint: String): String {
        return entrypoint.substring(0, entrypoint.indexOf("#"))
    }

    fun getEntrypointMethod(entrypoint: String): String {
        return entrypoint.substring(entrypoint.indexOf("#") + 1)
    }

    @JvmStatic
    fun unloadPlugins() {
        val classLoader = KoraLibraryLoader.classLoader
        for ((name, plugin) in KoraEntrypoint.DEPENDENCIES_MANAGER.getPlugins()) {
            if (plugin.unload.contains("#")) {
                LOGGER.info("Unloading plugin '{}'", name)
                val entrypoint = plugin.unload
                val entryClassName = getEntrypointClass(entrypoint)
                val entryMethodName = getEntrypointMethod(entrypoint)
                val entryClass = classLoader.loadClass(entryClassName)
                val method = entryClass.getMethod(entryMethodName)
                method(null)
            }
        }
    }
}