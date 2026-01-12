package com.github.cao.awa.kora.server.network.http.handler

import com.github.cao.awa.kora.server.network.http.context.KoraContext
import com.github.cao.awa.kora.server.network.http.control.abort.EndingEarlyException
import com.github.cao.awa.kora.server.network.http.control.abort.reason.AbortReason
import io.netty.handler.codec.http.HttpMethod
import kotlin.reflect.KClass

abstract class KoraHttpRequestHandler(val method: HttpMethod) {
    private val routes: MutableMap<String, KoraContext.() -> Any> = mutableMapOf()
    private val exceptionHandler: MutableMap<KClass<out Exception>, MutableMap<String, KoraContext.(AbortReason<out Exception>) -> Any>> =
        mutableMapOf()

    fun route(path: String, handler: KoraContext.() -> Any): KoraHttpRequestHandler {
        this.routes[path] = handler
        return this
    }

    fun routeExceptionHandler(
        path: String,
        type: KClass<out Exception>,
        handler: KoraContext.(AbortReason<out Exception>) -> Any
    ): KoraHttpRequestHandler {
        if (!this.exceptionHandler.containsKey(type)) {
            this.exceptionHandler[type] = mutableMapOf()
        }
        this.exceptionHandler[type]?.put(path, handler)
        return this
    }

    fun handle(context: KoraContext): Any {
        return this.routes[context.path()]?.let {
            it(context)
        } ?: EndingEarlyException.abort()
    }

    fun handleAbort(abortScope: KoraContext, abortReason: AbortReason<out Exception>): Any {
        return this.exceptionHandler[abortReason.exception::class]?.get(abortScope.path())?.let {
            it(abortScope, abortReason)
        } ?: EndingEarlyException.abort()
    }
}