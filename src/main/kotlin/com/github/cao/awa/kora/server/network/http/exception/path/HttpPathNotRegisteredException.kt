package com.github.cao.awa.kora.server.network.http.exception.path

import com.github.cao.awa.kora.server.network.http.exception.KoraServerException

class HttpPathNotRegisteredException(val msg: String): KoraServerException(msg) {
    companion object {
        @Throws(HttpPathNotRegisteredException::class)
        fun notFound(name: String, reason: String? = null): Nothing {
            val instructReason = if (reason == null) {
                ""
            } else {
                ", $reason"
            }
            throw HttpPathNotRegisteredException("No registered request handler found for pathing '${name}' (404 page not found$instructReason)")
        }
    }
}