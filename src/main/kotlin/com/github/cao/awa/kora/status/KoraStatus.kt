package com.github.cao.awa.kora.status

object KoraStatus {
    private val reloadListeners: MutableList<() -> Unit> = mutableListOf()
    private val reloadLocker: KoraStatusLocker = KoraStatusLocker()
    var running = true
    var reloading = false

    @JvmStatic
    fun reload() {
        this.reloading = true
        for (listener in this.reloadListeners) {
            listener()
        }
        this.reloadLocker.await()
        this.reloadLocker.clear()
    }

    @JvmStatic
    fun isReloading() = this.reloading

    @JvmStatic
    fun completeReload() {
        this.reloading = false
    }

    @JvmStatic
    fun isRunning() = this.running

    @JvmStatic
    fun stop() {
        this.running = false
    }

    fun registerReloadable(reloadable: Any) {
        this.reloadLocker.registerReloadable(reloadable)
    }

    fun completedLifecycle(reloadable: Any) {
        this.reloadLocker.completedLifecycle(reloadable)
    }

    fun registerReloadListener(listener: () -> Unit) {
        this.reloadListeners.add(listener)
    }
}