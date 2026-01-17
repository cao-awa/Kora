package com.github.cao.awa.kora.server.network.http.builder.error

import com.github.cao.awa.kora.server.network.http.adapter.KoraHttpInboundHandlerAdapter
import com.github.cao.awa.kora.server.network.http.context.KoraHttpContext
import com.github.cao.awa.kora.server.network.http.exception.abort.EndingEarlyException
import com.github.cao.awa.kora.server.network.http.control.abort.reason.AbortReason
import io.netty.handler.codec.http.HttpMethod
import kotlin.reflect.KClass

class KoraHttpRouteExceptionBuilder {
    private val method: HttpMethod
    private val path: String
    val routes: MutableMap<KClass<out Exception>, KoraHttpContext.(AbortReason<out Exception>) -> Any> = mutableMapOf()

    constructor(method: HttpMethod, path: String) {
        this.method = method
        this.path = path
    }

    @Suppress("unchecked_cast")
    inline fun <reified T: Exception, X: Any> abort(target: KClass<T>, noinline handler: KoraHttpContext.(AbortReason<T>) -> X): KoraHttpRouteExceptionBuilder {
        this.routes[target] = handler as KoraHttpContext.(AbortReason<out Exception>) -> Any
        return this
    }

    @Suppress("unchecked_cast")
    fun abort(handler: KoraHttpContext.(reason: AbortReason<EndingEarlyException>) -> Any): KoraHttpRouteExceptionBuilder {
        this.routes[EndingEarlyException::class] = handler as KoraHttpContext.(AbortReason<out Exception>) -> Any
        return this
    }

    fun applyRoute(adapter: KoraHttpInboundHandlerAdapter) {
        for ((type, handler) in this.routes) {
            adapter.pipeline.getHandler(this.method)?.routeExceptionHandler(this.path, type, handler)
        }
    }
}