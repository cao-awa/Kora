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
    val exceptionHandlers: MutableList<KoraHttpRouteExceptionBuilder> = mutableListOf()

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
            this.exceptionHandlers.add(it)
        }
    }

    inline fun <reified T : Any> get(noinline handler: KoraContext.() -> T): KoraHttpRouteGetExceptionBuilder {
        if (T::class == Unit::class) {
            error("HTTP method cannot missing response")
        }
        this.routes[HttpMethod.GET] = handler
        return KoraHttpRouteGetExceptionBuilder(this.path).also {
            this.exceptionHandlers.add(it)
        }
    }

    fun applyRoute(adapter: KoraHttpInboundHandlerAdapter) {
        this.routes[HttpMethod.POST]?.let { route ->
            routePost(adapter, this.path, route)
        }

        this.routes[HttpMethod.GET]?.let { route ->
            routeGet(adapter, this.path, route)
        }

        for (builder in this.exceptionHandlers) {
            builder.applyRoute(adapter)
        }
    }

    fun routePost(
        adapter: KoraHttpInboundHandlerAdapter,
        path: String,
        handler: KoraContext.() -> Any
    ) {
        adapter.handler.getHandler(HttpMethod.POST)?.route(path, handler)
    }

    fun routeGet(
        adapter: KoraHttpInboundHandlerAdapter,
        path: String,
        handler: KoraContext.() -> Any
    ) {
        adapter.handler.getHandler(HttpMethod.GET)?.route(path, handler)
    }
}