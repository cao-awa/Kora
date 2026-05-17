package com.github.cao.awa.kora.server.network.exception.abort

import com.github.cao.awa.kora.server.network.http.exception.KoraException

class UnexpectedBehaviorException(message: String? = null, cause: Throwable? = null): KoraException(message, cause) {
    companion object {
        fun abort(): Nothing = throw UnexpectedBehaviorException("Control stream lifecycle unexpected aborting")
    }
}