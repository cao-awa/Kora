package com.github.cao.awa.kora.status

object KoraStatus {
    var running = true
    var reloading = false

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
}