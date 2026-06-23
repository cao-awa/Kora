package com.github.cao.awa.kora.server.network.http.builder.error

import com.github.cao.awa.kora.server.network.http.adapter.KoraHttpInboundHandlerAdapter
import com.github.cao.awa.kora.server.network.http.context.abort.KoraAbortHttpContext
import io.netty.handler.codec.http.HttpMethod
import kotlin.reflect.KClass

class KoraHttpRouteExceptionBuilder {
    private val method: HttpMethod
    private val path: String
    val routes: MutableMap<KClass<out Throwable>, KoraAbortHttpContext.(Throwable) -> Any> = mutableMapOf()

    constructor(method: HttpMethod, path: String) {
        this.method = method
        this.path = path
    }

    @Suppress("unchecked_cast")
    inline fun <reified T: Throwable, X: Any> ifAbort(target: KClass<T>, noinline handler: KoraAbortHttpContext.(T) -> X): KoraHttpRouteExceptionBuilder {
        if (this.routes.containsKey(target)) {
            throw IllegalStateException("Already presenting an exception handler for type '${target.simpleName}'")
        }
        this.routes[target] = handler as KoraAbortHttpContext.(Throwable) -> Any
        return this
    }

    fun applyRoute(adapter: KoraHttpInboundHandlerAdapter) {
        for ((type, handler) in this.routes) {
            adapter.pipeline.getHandler(this.method)?.routeExceptionHandler(this.path, type, handler)
        }
    }
}