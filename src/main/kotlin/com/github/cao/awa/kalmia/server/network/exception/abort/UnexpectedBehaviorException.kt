package com.github.cao.awa.kalmia.server.network.exception.abort

import com.github.cao.awa.kalmia.server.network.http.exception.KalmiaException

class UnexpectedBehaviorException(message: String? = null, cause: Throwable? = null): KalmiaException(message, cause) {
    companion object {
        fun abort(): Nothing = throw UnexpectedBehaviorException("Control stream lifecycle unexpected aborting")
    }
}