package com.github.cao.awa.kora.server.network.http.placeholder.url.type.validator.exception

class TypedHttpUrlValidateException(val msg: String): RuntimeException(msg) {
    companion object {
        fun failed(msg: String): Nothing = throw TypedHttpUrlValidateException(msg)
    }
}