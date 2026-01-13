package com.github.cao.awa.kora.server.network.http.builder

import com.github.cao.awa.kora.server.network.http.builder.route.KoraHttpServerRouteBuilder
import com.github.cao.awa.kora.server.network.http.adapter.KoraHttpInboundHandlerAdapter

class KoraHttpServerBuilder {
    private val routes: MutableMap<String, MutableList<KoraHttpServerRouteBuilder>> = mutableMapOf()

    constructor(builder: KoraHttpServerBuilder.() -> Unit) {
        builder(this)
    }

    fun route(targetPath: String, handler: KoraHttpServerRouteBuilder.() -> Unit) {
        val path = targetPath.let {
            if (it.endsWith("/")) {
                it.substring(0, it.length - 1)
            } else {
                it
            }
        }

        if (!this.routes.containsKey(path)) {
            this.routes[path] = mutableListOf()
            this.routes[path]!!.add(KoraHttpServerRouteBuilder(path, handler))
        } else {
            error("Duplicated route path: $path")
        }
    }

    fun applyRoute(adapter: KoraHttpInboundHandlerAdapter) {
        for ((key, value) in this.routes) {
            for (builder in value) {
                builder.applyRoute(adapter)
            }
        }
    }
}

fun http(handler: KoraHttpServerBuilder.() -> Unit): KoraHttpServerBuilder {
    return KoraHttpServerBuilder(handler)
}