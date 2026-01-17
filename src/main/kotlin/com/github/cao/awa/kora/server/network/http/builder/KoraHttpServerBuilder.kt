package com.github.cao.awa.kora.server.network.http.builder

import com.github.cao.awa.kora.server.network.http.builder.route.KoraHttpServerRouteBuilder
import com.github.cao.awa.kora.server.network.http.adapter.KoraHttpInboundHandlerAdapter
import java.net.URLEncoder

class KoraHttpServerBuilder {
    private val routes: MutableMap<String, KoraHttpServerRouteBuilder> = mutableMapOf()

    constructor(builder: KoraHttpServerBuilder.() -> Unit) {
        builder(this)
    }

    fun route(targetPath: String, handler: KoraHttpServerRouteBuilder.() -> Unit) {
        var path = targetPath

        path = if (path.endsWith("/")) {
            path.substring(0, path.length - 1)
        } else {
            path
        }

        path = if (path.startsWith("/")) {
            path.substring(1, path.length)
        } else {
            path
        }

        // Encode the path and replace connecting symbol to '%20' .
        path = "/${URLEncoder.encode(path, "UTF-8")}"
            .replace("+", "%20")

        if (!this.routes.containsKey(path)) {
            this.routes[path] = KoraHttpServerRouteBuilder(path, handler)
        } else {
            error("Duplicated route path: $path")
        }
    }

    fun applyRoute(adapter: KoraHttpInboundHandlerAdapter) {
        for ((key, builder) in this.routes) {
            builder.applyRoute(adapter)
        }
    }
}

fun http(handler: KoraHttpServerBuilder.() -> Unit): KoraHttpServerBuilder {
    return KoraHttpServerBuilder(handler)
}