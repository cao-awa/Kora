package com.github.cao.awa.kora.server.network.http.builder.route

import com.github.cao.awa.kora.server.network.http.builder.error.KoraHttpRouteExceptionBuilder
import com.github.cao.awa.kora.server.network.http.builder.error.get.KoraHttpRouteGetExceptionBuilder
import com.github.cao.awa.kora.server.network.http.builder.error.post.KoraHttpRoutePostExceptionBuilder
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

    inline fun <reified T : Any> post(noinline handler: KoraContext.() -> T): KoraHttpRoutePostExceptionBuilder {
        if (T::class == Unit::class) {
            error("HTTP method cannot missing response")
        }
        this.routes[HttpMethod.POST] = handler
        return KoraHttpRoutePostExceptionBuilder(this.path).also {
            this.exceptionHandlers[HttpMethod.POST] = it
        }
    }

    inline fun <reified T : Any> get(noinline handler: KoraContext.() -> T): KoraHttpRouteGetExceptionBuilder {
        if (T::class == Unit::class) {
            error("HTTP method cannot missing response")
        }
        this.routes[HttpMethod.GET] = handler
        return KoraHttpRouteGetExceptionBuilder(this.path).also {
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