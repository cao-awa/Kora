package com.github.cao.awa.kora.server.network.http.exception

import com.github.cao.awa.kora.server.network.KoraNetworkConfig

open class KoraException(message: String?, cause: Throwable?): RuntimeException(message, cause) {
    companion object {
        private val UNASSIGNED_STACK: Array<StackTraceElement?> = arrayOfNulls(0)
    }

    override fun fillInStackTrace(): Throwable {
        return if (!KoraNetworkConfig.responseFillStacktrace) {
            this.stackTrace = UNASSIGNED_STACK
            this
        } else {
            super.fillInStackTrace()
        }
    }
}