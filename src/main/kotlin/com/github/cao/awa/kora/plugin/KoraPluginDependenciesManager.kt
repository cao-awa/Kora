package com.github.cao.awa.kora.plugin

import com.github.cao.awa.kora.KoraEntrypoint

class KoraPluginDependenciesManager {
    companion object {
        private val EMPTY_DEPENDENCIES: Array<String> = emptyArray()
    }

    private val plugins: MutableMap<String, KoraPlugin> = HashMap()
    private val loadedPlugin: MutableList<String> = ArrayList()

    fun onPluginLoad(name: String) {
        this.loadedPlugin.add(name)
    }

    fun isPluginLoaded(name: String): Boolean {
        return loadedPlugin.contains(name)
    }

    fun addPlugin(name: String, entrypoint: String, dependency: Array<String>, fallback: String) {
        this.plugins[name] = KoraPlugin(name, entrypoint, dependency, fallback)
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
}

fun markPluginLoaded(name: String) {
    KoraEntrypoint.DEPENDENCIES_MANAGER.onPluginLoad(name)
}

fun isPluginLoaded(name: String): Boolean {
    return KoraEntrypoint.DEPENDENCIES_MANAGER.isPluginLoaded(name)
}