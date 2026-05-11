package com.github.cao.awa.kora.server.network.http.placeholder.url.exception

class TypedHttpUrlMissingException(val msg: String): RuntimeException(msg) {
    companion object{
        fun missing(msg: String): Nothing = throw TypedHttpUrlMissingException(msg)
    }
}