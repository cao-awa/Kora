package com.github.cao.awa.kora.server.network.http.exception.abort

import com.github.cao.awa.kora.server.network.http.KoraHttpServer
import com.github.cao.awa.kora.server.network.http.exception.KoraHttpException

class EndingEarlyException(message: String? = null, cause: Throwable? = null): KoraHttpException(message, cause) {
    companion object {
        private val UNASSIGNED_STACK: Array<StackTraceElement?> = arrayOfNulls(0)

        fun abort() {
            throw EndingEarlyException("Control stream lifecycle aborting")
        }
    }

    override fun fillInStackTrace(): Throwable {
        return if (KoraHttpServer.fastAbort) {
            this.stackTrace = UNASSIGNED_STACK
            this
        } else {
            super.fillInStackTrace()
        }
    }
}