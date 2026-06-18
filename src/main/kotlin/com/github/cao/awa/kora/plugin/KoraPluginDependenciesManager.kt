package com.github.cao.awa.kora.plugin

import com.github.cao.awa.kora.entrypoint.KoraEntrypoint
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class KoraPluginDependenciesManager {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger("KoraPluginDependenciesManager")
        private val EMPTY_DEPENDENCIES: Array<String> = emptyArray()
    }

    private val plugins: MutableMap<String, KoraPlugin> = HashMap()
    private val loadedPlugin: MutableMap<String, KoraPlugin> = HashMap()
    private val loadedEntrypoint: MutableMap<String, KoraPlugin?> = HashMap()
    private val cleaners: MutableMap<String, () -> Unit> = HashMap()

    fun onPluginLoad(plugin: KoraPlugin) {
        this.loadedPlugin[plugin.name] = plugin
    }

    fun onEntrypointLoad(name: String, plugin: KoraPlugin?) {
        this.loadedEntrypoint[name] = plugin
    }

    fun isPluginLoaded(name: String): Boolean {
        return loadedPlugin[name] != null
    }

    fun isPluginLoaded(plugin: KoraPlugin): Boolean {
        return isPluginLoaded(plugin.name)
    }

    fun addPlugin(name: String, entrypoint: String, dependency: Array<String>, fallback: String, reload: String) {
        this.plugins[name] = KoraPlugin(name, entrypoint, dependency, fallback, reload)
    }

    fun getPlugins(): Map<String, KoraPlugin> {
        return this.plugins
    }

    fun getLoadedPlugins(): Map<String, KoraPlugin> {
        return this.loadedPlugin
    }

    fun getLoadedEntrypoint(): Map<String, KoraPlugin?> {
        return this.loadedEntrypoint
    }

    fun isDependsOn(plugin: String, dependency: String): Boolean {
        val dependencies = this.plugins[plugin]
        if (dependencies != null) {
            return dependencies.dependsOn.contains(dependency)
        }
        return false
    }

    fun getDependsOn(plugin: String): Array<String> {
        return this.plugins[plugin]?.dependsOn ?: EMPTY_DEPENDENCIES
    }

    fun getPlugin(name: String): KoraPlugin? {
        return this.plugins[name]
    }

    fun registerCleaner(name: String, cleaner: () -> Unit) {
        LOGGER.info("Registering resource cleaner for '$name'")
        this.cleaners[name] = cleaner
    }

    fun getCleaners(): Map<String, () -> Unit> {
        return this.cleaners
    }

    fun clearCleaners() {
        this.cleaners.clear()
    }
}

fun markPluginLoaded(name: String) {
    val dependenciesManager = KoraEntrypoint.DEPENDENCIES_MANAGER
    val plugin = dependenciesManager.getPlugin(name)
    if (plugin != null) {
        markPluginLoaded(plugin)
    } else {
        throw IllegalStateException("Plugin '$name' doesn't be loaded with completed plugin, it's unnamed plugin?")
    }
}

fun markPluginLoaded(plugin: KoraPlugin) {
    KoraEntrypoint.DEPENDENCIES_MANAGER.onPluginLoad(plugin)
}

fun markEntrypointLoaded(name: String, plugin: KoraPlugin?) {
    KoraEntrypoint.DEPENDENCIES_MANAGER.onEntrypointLoad(name, plugin)
}

fun isPluginLoaded(name: String): Boolean {
    return KoraEntrypoint.DEPENDENCIES_MANAGER.isPluginLoaded(name)
}

fun registerCleaner(name: String, cleaner: () -> Unit) {
    KoraEntrypoint.DEPENDENCIES_MANAGER.registerCleaner(name, cleaner)
}