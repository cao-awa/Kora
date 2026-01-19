package com.github.cao.awa.kora.server.network.ws.builder

import com.github.cao.awa.kora.server.network.http.adapter.KoraHttpInboundHandlerAdapter
import com.github.cao.awa.kora.server.network.ws.adapter.KoraWebSocketFrameAdapter
import com.github.cao.awa.kora.server.network.ws.builder.route.KoraWebSocketRouteBuilder
import java.net.URLEncoder

class KoraWebsocketServerBuilder {
    private val routes: MutableMap<String, KoraWebSocketRouteBuilder> = mutableMapOf()

    constructor(builder: KoraWebsocketServerBuilder.() -> Unit) {
        builder(this)
    }

    fun route(targetPath: String, handler: KoraWebSocketRouteBuilder.() -> Unit) {
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
            this.routes[path] = KoraWebSocketRouteBuilder(path, handler)
        } else {
            error("Duplicated route path: $path")
        }
    }

    fun applyRoute(adapter: KoraWebSocketFrameAdapter) {
        for ((key, builder) in this.routes) {
            builder.applyRoute(adapter)
        }
    }
}

fun websocket(handler: KoraWebsocketServerBuilder.() -> Unit): KoraWebsocketServerBuilder {
    return KoraWebsocketServerBuilder(handler)
}