package com.github.cao.awa.kora.server.network.http.builder.error

import com.github.cao.awa.kora.server.network.http.handler.adapter.KoraHttpInboundHandlerAdapter

abstract class KoraHttpRouteExceptionBuilder {
    abstract fun applyRoute(adapter: KoraHttpInboundHandlerAdapter)
}