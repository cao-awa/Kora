package com.github.cao.awa.kalmia.server.network.http.placeholder.url.exception

class TypedHttpUrlMissingException(val msg: String): RuntimeException(msg) {
    companion object{
        fun missing(msg: String): Nothing = throw TypedHttpUrlMissingException(msg)
    }
}