package com.github.cao.awa.kora.server.network.http.builder

import com.github.cao.awa.kora.server.network.http.handler.KoraHttpRequestHandler

class KoraHttpServerBuilder() {
    constructor(builder: KoraHttpServerBuilder.() -> Unit) : this() {
        builder(this)
    }

    fun build(): KoraHttpRequestHandler {
        return KoraHttpRequestHandler()
    }
}