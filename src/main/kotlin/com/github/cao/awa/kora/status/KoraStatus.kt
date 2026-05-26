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
            listener()
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
            listener()
        }
        this.locker.await()
        this.locker.clear()
    }

    fun registerLifecycle(reloadable: Any) {
        this.locker.registerLifecycle(reloadable)
    }

    fun completedLifecycle(reloadable: Any) {
        this.locker.completedLifecycle(reloadable)
    }

    fun registerReloadListener(listener: () -> Unit) {
        this.reloadListeners.add(listener)
    }

    fun registerStopListener(listener: () -> Unit) {
        this.stopListeners.add(listener)
    }
}