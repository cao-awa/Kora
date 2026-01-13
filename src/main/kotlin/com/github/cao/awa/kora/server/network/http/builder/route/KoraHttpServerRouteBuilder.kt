package com.github.cao.awa.kora.server.network.http.builder.route

import com.github.cao.awa.kora.server.network.http.builder.error.KoraHttpRouteExceptionBuilder
import com.github.cao.awa.kora.server.network.http.adapter.KoraHttpInboundHandlerAdapter
import com.github.cao.awa.kora.server.network.http.context.KoraContext
import io.netty.handler.codec.http.HttpMethod

class KoraHttpServerRouteBuilder {
    val path: String
    val routes: MutableMap<HttpMethod, KoraContext.() -> Any> = mutableMapOf()
    val exceptionHandlers: MutableMap<HttpMethod, KoraHttpRouteExceptionBuilder> = mutableMapOf()

    constructor(path: String, builder: KoraHttpServerRouteBuilder.() -> Unit) {
        this.path = path
        builder(this)
    }

    inline fun <reified T : Any> post(noinline handler: KoraContext.() -> T): KoraHttpRouteExceptionBuilder {
        if (T::class == Unit::class) {
            error("HTTP method cannot missing response")
        }
        if (this.routes.containsKey(HttpMethod.POST)) {
            error("Duplicated HTTP POST handler")
        }
        this.routes[HttpMethod.POST] = handler
        return KoraHttpRouteExceptionBuilder(HttpMethod.POST, this.path).also {
            this.exceptionHandlers[HttpMethod.POST] = it
        }
    }

    inline fun <reified T : Any> get(noinline handler: KoraContext.() -> T): KoraHttpRouteExceptionBuilder {
        if (T::class == Unit::class) {
            error("HTTP method cannot missing response")
        }
        if (this.routes.containsKey(HttpMethod.POST)) {
            error("Duplicated HTTP GET handler")
        }
        this.routes[HttpMethod.GET] = handler
        return KoraHttpRouteExceptionBuilder(HttpMethod.GET, this.path).also {
            this.exceptionHandlers[HttpMethod.GET] = it
        }
    }

    fun applyRoute(adapter: KoraHttpInboundHandlerAdapter) {
        this.routes[HttpMethod.POST]?.let { route ->
            routePost(adapter, route)
        }

        this.routes[HttpMethod.GET]?.let { route ->
            routeGet(adapter, route)
        }

        this.exceptionHandlers[HttpMethod.POST]?.let { route ->
            routeExceptionHandler(adapter, route)
        }

        this.exceptionHandlers[HttpMethod.GET]?.let { route ->
            routeExceptionHandler(adapter, route)
        }
    }

    fun routePost(
        adapter: KoraHttpInboundHandlerAdapter,
        handler: KoraContext.() -> Any
    ) {
        adapter.pipeline.getHandler(HttpMethod.POST)?.route(this.path, handler)
    }

    fun routeExceptionHandler(
        adapter: KoraHttpInboundHandlerAdapter,
        handler: KoraHttpRouteExceptionBuilder
    ) {
        handler.applyRoute(adapter)
    }

    fun routeGet(
        adapter: KoraHttpInboundHandlerAdapter,
        handler: KoraContext.() -> Any
    ) {
        adapter.pipeline.getHandler(HttpMethod.GET)?.route(this.path, handler)
    }
}