package com.github.cao.awa.kora.status

object KoraStatus {
    private val reloadListeners: MutableList<() -> Unit> = mutableListOf()
    private val stopListeners: MutableList<() -> Unit> = mutableListOf()
    private val locker: KoraStatusLocker = KoraStatusLocker()
    var running = true
    var reloading = false

    @JvmStatic
    fun reload() {
        this.reloading = true
        for (listener in this.reloadListeners) {
            try {
                listener()
            } catch (_: Exception) {

            }
        }
        this.locker.await()
        this.locker.clear()
    }

    @JvmStatic
    fun isReloading() = this.reloading

    @JvmStatic
    fun completeReload() {
        synchronized(this) {
            this.reloading = false
        }
    }

    @JvmStatic
    fun isRunning() = this.running

    @JvmStatic
    fun stop() {
        this.running = false
        for (listener in this.stopListeners) {
            try {
                listener()
            } catch (_: Exception) {

            }
        }
        this.locker.await()
        this.locker.clear()
    }

    fun registerLifecycle(name: String, reloadable: Any) {
        this.locker.registerLifecycle(name, reloadable)
    }

    fun completedLifecycle(name: String) {
        this.locker.completedLifecycle(name)
    }

    fun completedLifecycle(reloadableHolder: Any) {
        this.locker.completedLifecycle(reloadableHolder)
    }

    fun registerReloadListener(listener: () -> Unit) {
        this.reloadListeners.add(listener)
    }

    fun registerStopListener(listener: () -> Unit) {
        this.stopListeners.add(listener)
    }

    fun registeredLifecycle(): Map<String, Any> {
        return this.locker.registeredLifecycle()
    }
}