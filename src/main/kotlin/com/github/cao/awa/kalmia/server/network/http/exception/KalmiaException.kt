package com.github.cao.awa.kalmia.server.network.http.exception

import com.github.cao.awa.kalmia.server.network.KalmiaNetworkConfig

open class KalmiaException(message: String?, cause: Throwable?): RuntimeException(message, cause) {
    companion object {
        private val UNASSIGNED_STACK: Array<StackTraceElement?> = arrayOfNulls(0)
    }

    override fun fillInStackTrace(): Throwable {
        return if (!KalmiaNetworkConfig.responseFillStacktrace) {
            this.stackTrace = UNASSIGNED_STACK
            this
        } else {
            super.fillInStackTrace()
        }
    }
}