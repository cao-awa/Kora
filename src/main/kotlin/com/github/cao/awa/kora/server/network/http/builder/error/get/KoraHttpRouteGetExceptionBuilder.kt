package com.github.cao.awa.kora.server.network.http.builder.error.get

import com.github.cao.awa.kora.server.network.http.builder.error.KoraHttpRouteExceptionBuilder
import com.github.cao.awa.kora.server.network.http.context.KoraContext
import com.github.cao.awa.kora.server.network.http.control.end.EndingEarlyException
import com.github.cao.awa.kora.server.network.http.handler.adapter.KoraHttpInboundHandlerAdapter
import kotlin.collections.iterator
import kotlin.reflect.KClass

class KoraHttpRouteGetExceptionBuilder: KoraHttpRouteExceptionBuilder {
    private val path: String
    val routes: MutableMap<KClass<out Exception>, KoraContext.() -> Any> = mutableMapOf()

    constructor(path: String) {
        this.path = path
    }

    inline fun <reified T: Exception, X: Any> targetError(target: KClass<T>, noinline handler: KoraContext.() -> X): KoraHttpRouteGetExceptionBuilder {
        this.routes[target] = handler
        return this
    }

    fun abort(handler: KoraContext.() -> Any): KoraHttpRouteGetExceptionBuilder {
        this.routes[EndingEarlyException::class] = handler
        return this
    }

    override fun applyRoute(adapter: KoraHttpInboundHandlerAdapter) {
        for ((type, handler) in this.routes) {
            adapter.handler.routeGetException(this.path, type, handler)
        }
    }
}