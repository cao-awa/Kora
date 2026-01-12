package com.github.cao.awa.kora.server.network.http.builder.error.post

import com.github.cao.awa.kora.server.network.http.builder.error.KoraHttpRouteExceptionBuilder
import com.github.cao.awa.kora.server.network.http.context.KoraContext
import com.github.cao.awa.kora.server.network.http.control.abort.EndingEarlyException
import com.github.cao.awa.kora.server.network.http.control.abort.reason.AbortReason
import com.github.cao.awa.kora.server.network.http.handler.adapter.KoraHttpInboundHandlerAdapter
import kotlin.collections.iterator
import kotlin.reflect.KClass

class KoraHttpRoutePostExceptionBuilder: KoraHttpRouteExceptionBuilder {
    private val path: String
    val routes: MutableMap<KClass<out Exception>, KoraContext.(AbortReason<out Exception>) -> Any> = mutableMapOf()

    constructor(path: String) {
        this.path = path
    }

    @Suppress("unchecked_cast")
    inline fun <reified T: Exception, X: Any> targetError(target: KClass<T>, noinline handler: KoraContext.(T) -> X): KoraHttpRoutePostExceptionBuilder {
        this.routes[target] = handler as KoraContext.(AbortReason<out Exception>) -> Any
        return this
    }

    @Suppress("unchecked_cast")
    fun abort(handler: KoraContext.(EndingEarlyException) -> Any): KoraHttpRoutePostExceptionBuilder {
        this.routes[EndingEarlyException::class] = handler as KoraContext.(AbortReason<out Exception>) -> Any
        return this
    }

    override fun applyRoute(adapter: KoraHttpInboundHandlerAdapter) {
        for ((type, handler) in this.routes) {
            adapter.handler.routePostException(this.path, type, handler)
        }
    }
}