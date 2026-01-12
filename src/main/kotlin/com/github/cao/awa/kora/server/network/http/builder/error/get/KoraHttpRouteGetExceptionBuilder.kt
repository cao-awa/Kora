package com.github.cao.awa.kora.server.network.http.builder.error.get

import com.github.cao.awa.kora.server.network.http.builder.error.KoraHttpRouteExceptionBuilder
import com.github.cao.awa.kora.server.network.http.context.KoraContext
import com.github.cao.awa.kora.server.network.http.control.abort.EndingEarlyException
import com.github.cao.awa.kora.server.network.http.control.abort.reason.AbortReason
import com.github.cao.awa.kora.server.network.http.adapter.KoraHttpInboundHandlerAdapter
import io.netty.handler.codec.http.HttpMethod
import kotlin.collections.iterator
import kotlin.reflect.KClass

class KoraHttpRouteGetExceptionBuilder: KoraHttpRouteExceptionBuilder {
    private val path: String
    val routes: MutableMap<KClass<out Exception>, KoraContext.(AbortReason<out Exception>) -> Any> = mutableMapOf()

    constructor(path: String) {
        this.path = path
    }

    @Suppress("unchecked_cast")
    inline fun <reified T: Exception, X: Any> targetError(target: KClass<T>, noinline handler: KoraContext.(AbortReason<T>) -> X): KoraHttpRouteGetExceptionBuilder {
        this.routes[target] = handler as KoraContext.(AbortReason<out Exception>) -> Any
        return this
    }

    @Suppress("unchecked_cast")
    fun abort(handler: KoraContext.(reason: AbortReason<EndingEarlyException>) -> Any): KoraHttpRouteGetExceptionBuilder {
        this.routes[EndingEarlyException::class] = handler as KoraContext.(AbortReason<out Exception>) -> Any
        return this
    }

    override fun applyRoute(adapter: KoraHttpInboundHandlerAdapter) {
        for ((type, handler) in this.routes) {
            adapter.pipeline.getHandler(HttpMethod.GET)?.routeExceptionHandler(this.path, type, handler)
        }
    }
}