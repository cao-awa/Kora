package com.github.cao.awa.kora.server.network.http.control.abort

import com.github.cao.awa.kora.server.network.http.control.KoraException

class EndingEarlyException(message: String? = null, cause: Throwable? = null): KoraException(message, cause) {
    companion object {
        fun abort() {
            throw EndingEarlyException("Control stream lifecycle aborting")
        }
    }
}