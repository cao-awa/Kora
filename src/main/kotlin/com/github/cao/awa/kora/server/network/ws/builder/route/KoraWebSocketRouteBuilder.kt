package com.github.cao.awa.kora.server.network.ws.builder.route

import com.github.cao.awa.kora.server.network.ws.adapter.KoraWebSocketFrameAdapter
import com.github.cao.awa.kora.server.network.ws.builder.error.KoraWebSocketRouteExceptionBuilder
import com.github.cao.awa.kora.server.network.ws.context.KoraWebSocketContext

class KoraWebSocketRouteBuilder {
    private val path: String
    val routes: MutableMap<String, KoraWebSocketContext.() -> Any> = mutableMapOf()
    val exceptionHandlers: MutableMap<String, KoraWebSocketRouteExceptionBuilder> = mutableMapOf()

    constructor(path: String, builder: KoraWebSocketRouteBuilder.() -> Unit) {
        this.path = path
        builder(this)
    }

    fun applyRoute(adapter: KoraWebSocketFrameAdapter) {
        for ((path, router) in this.routes) {
            adapter.pipeline.route(path, router)
        }
    }
}