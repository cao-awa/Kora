package com.github.cao.awa.kora.server.network.exception.abort

import com.github.cao.awa.kora.server.network.KoraNetworkConfig

class EndingEarlyException(message: String? = null, cause: Throwable? = null): RuntimeException(message, cause) {
    companion object {
        private val UNASSIGNED_STACK: Array<StackTraceElement?> = arrayOfNulls(0)

        fun abort() {
            throw EndingEarlyException("Control stream lifecycle aborting")
        }
    }

    override fun fillInStackTrace(): Throwable {
        return if (KoraNetworkConfig.fastAbort) {
            this.stackTrace = UNASSIGNED_STACK
            this
        } else {
            super.fillInStackTrace()
        }
    }
}