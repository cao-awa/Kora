package com.github.cao.awa.kora.server.network.ws.builder.route

import com.github.cao.awa.kora.server.network.ws.builder.error.KoraWebSocketRouteExceptionBuilder
import com.github.cao.awa.kora.server.network.ws.context.KoraWebSocketContext
import com.github.cao.awa.kora.server.network.ws.phase.KoraWebSocketPhase
import com.github.cao.awa.kora.server.network.ws.adapter.protocol.KoraWebSocketServerProtocolAdapter

class KoraWebSocketRouteBuilder {
    private val path: String
    val routes: MutableMap<KoraWebSocketPhase, KoraWebSocketContext.() -> Any> = mutableMapOf()
    val exceptionHandlers: MutableMap<KoraWebSocketPhase, KoraWebSocketRouteExceptionBuilder> = mutableMapOf()

    constructor(path: String, builder: KoraWebSocketRouteBuilder.() -> Unit) {
        this.path = path
        builder(this)
    }

    fun onMessage(builder: KoraWebSocketContext.() -> Any) {
        this.routes[KoraWebSocketPhase.MESSAGE] = builder
    }

    fun applyRoute(adapter: KoraWebSocketServerProtocolAdapter) {
        for ((phase, router) in this.routes) {
            adapter.pipeline.route(this.path, phase, router)
        }
    }
}