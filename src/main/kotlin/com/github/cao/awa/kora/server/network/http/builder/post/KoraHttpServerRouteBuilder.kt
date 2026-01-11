package com.github.cao.awa.kora.server.network.http.builder.post

import com.github.cao.awa.kora.server.network.http.handler.KoraHttpInboundHandlerAdapter
import com.github.cao.awa.kora.server.network.response.KoraContext
import io.netty.handler.codec.http.HttpMethod

class KoraHttpServerRouteBuilder {
    private val path: String
    val routes: MutableMap<HttpMethod, KoraContext.() -> Any> = mutableMapOf()

    constructor(path: String, builder: KoraHttpServerRouteBuilder.() -> Unit) {
        this.path = path
        builder(this)
    }

    inline fun <reified T: Any> post(noinline handler: KoraContext.() -> T) {
        if (T::class == Unit::class) {
            error("HTTP method cannot missing response")
        }
        this.routes[HttpMethod.POST] = handler
    }

    inline fun <reified T: Any> get(noinline handler: KoraContext.() -> T) {
        if (T::class == Unit::class) {
            error("HTTP method cannot missing response")
        }
        this.routes[HttpMethod.GET] = handler
    }

    fun applyRoute(adapter: KoraHttpInboundHandlerAdapter) {
        this.routes[HttpMethod.POST]?.let { route ->
            adapter.handler.routePost(this.path, route)
        }

        this.routes[HttpMethod.GET]?.let { route ->
            adapter.handler.routeGet(this.path, route)
        }
    }
}