package com.github.cao.awa.kora.server.network.http.builder

class KoraHttpServerBuilder() {
    constructor(builder: KoraHttpServerBuilder.() -> Unit) : this() {
        builder(this)
    }

    fun build() {

    }
}