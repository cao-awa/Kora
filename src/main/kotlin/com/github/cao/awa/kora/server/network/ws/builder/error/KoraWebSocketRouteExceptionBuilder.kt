package com.github.cao.awa.kora.server.network.ws.builder.error

import com.github.cao.awa.kora.server.network.exception.abort.EndingEarlyException
import com.github.cao.awa.kora.server.network.control.abort.reason.AbortReason
import com.github.cao.awa.kora.server.network.ws.context.abort.KoraAbortWebSocketContext
import com.github.cao.awa.kora.server.network.ws.adapter.protocol.KoraWebSocketServerProtocolAdapter
import io.netty.handler.codec.http.HttpMethod
import kotlin.reflect.KClass

class KoraWebSocketRouteExceptionBuilder {
    private val path: String
    val routes: MutableMap<KClass<out Throwable>, KoraAbortWebSocketContext.(AbortReason<out Throwable>) -> Any> = mutableMapOf()

    constructor(method: HttpMethod, path: String) {
        this.path = path
    }

    @Suppress("unchecked_cast")
    inline fun <reified T: Throwable, X: Any> abort(target: KClass<T>, noinline handler: KoraAbortWebSocketContext.(AbortReason<T>) -> X): KoraWebSocketRouteExceptionBuilder {
        this.routes[target] = handler as KoraAbortWebSocketContext.(AbortReason<out Throwable>) -> Any
        return this
    }

    @Suppress("unchecked_cast")
    fun abort(handler: KoraAbortWebSocketContext.(reason: AbortReason<EndingEarlyException>) -> Any): KoraWebSocketRouteExceptionBuilder {
        this.routes[EndingEarlyException::class] = handler as KoraAbortWebSocketContext.(AbortReason<out Throwable>) -> Any
        return this
    }

    fun applyRoute(adapter: KoraWebSocketServerProtocolAdapter) {
        for ((type, handler) in this.routes) {
            adapter.pipeline.routeExceptionHandler(this.path, type, handler)
        }
    }
}