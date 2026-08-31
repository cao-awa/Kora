package com.github.cao.awa.kalmia.server.network.http.argument.type.validator.exception

class TypedHttpArgumentValidateException(val msg: String): RuntimeException(msg) {
    companion object {
        fun failed(msg: String): Nothing = throw TypedHttpArgumentValidateException(msg)
    }
}