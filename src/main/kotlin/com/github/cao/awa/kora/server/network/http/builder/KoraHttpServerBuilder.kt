package com.github.cao.awa.kora.server.network.http.builder

import com.github.cao.awa.kora.server.network.control.abort.reason.AbortReason
import com.github.cao.awa.kora.server.network.http.builder.route.KoraHttpServerRouteBuilder
import com.github.cao.awa.kora.server.network.http.adapter.KoraHttpInboundHandlerAdapter
import com.github.cao.awa.kora.server.network.http.asset.KoraHttpAssetsManager
import com.github.cao.awa.kora.server.network.http.context.abort.KoraAbortHttpContext
import com.github.cao.awa.kora.server.network.http.exception.KoraServerException
import java.net.URLEncoder
import kotlin.reflect.KClass

class KoraHttpServerBuilder {
    private val routes: MutableMap<String, KoraHttpServerRouteBuilder> = mutableMapOf()
    private var assetsPath: String = ""
    val abortHandlers: MutableMap<KClass<out Throwable>, KoraAbortHttpContext.(AbortReason<out Throwable>) -> Any> = mutableMapOf()

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
        path = "${URLEncoder.encode(path, "UTF-8")}"
            .replace("+", "%20")
            .replace("%2F", "/")
            .replace("%7B","{")
            .replace("%7D","}")

        if (!this.routes.containsKey(path)) {
            this.routes[path] = KoraHttpServerRouteBuilder(path, handler)
        } else {
            error("Duplicated route path: $path")
        }
    }

    fun assets(path: String) {
        this.assetsPath = path
    }

    fun applyRoute(adapter: KoraHttpInboundHandlerAdapter) {
        adapter.pipeline.setAssetsPath(this.assetsPath)
        for ((key, builder) in this.routes) {
            builder.applyRoute(adapter)
        }
    }

    @Suppress("unchecked_cast")
    fun <T : KoraServerException> ifAbort(type: KClass<out T>, context: KoraAbortHttpContext.(AbortReason<T>) -> Any) {
        this.abortHandlers[type] = context as KoraAbortHttpContext.(AbortReason<out Throwable>) -> Any
    }
}

fun http(handler: KoraHttpServerBuilder.() -> Unit): KoraHttpServerBuilder {
    return KoraHttpServerBuilder(handler)
}