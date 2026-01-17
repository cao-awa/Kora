package com.github.cao.awa.kora.server.network.http.handler

import com.github.cao.awa.kora.server.network.http.context.KoraHttpContext
import com.github.cao.awa.kora.server.network.http.context.abort.KoraAbortHttpContext
import com.github.cao.awa.kora.server.network.http.exception.abort.EndingEarlyException
import com.github.cao.awa.kora.server.network.http.control.abort.reason.AbortReason
import io.netty.handler.codec.http.HttpMethod
import kotlin.reflect.KClass

abstract class KoraHttpRequestHandler(val method: HttpMethod) {
    private val routes: MutableMap<String, KoraHttpContext.() -> Any> = mutableMapOf()
    private val exceptionHandler: MutableMap<KClass<out Throwable>, MutableMap<String, KoraAbortHttpContext.(AbortReason<out Throwable>) -> Any>> =
        mutableMapOf()

    fun route(path: String, handler: KoraHttpContext.() -> Any): KoraHttpRequestHandler {
        this.routes[path] = handler
        return this
    }

    fun routeExceptionHandler(
        path: String,
        type: KClass<out Throwable>,
        handler: KoraAbortHttpContext.(AbortReason<out Throwable>) -> Any
    ): KoraHttpRequestHandler {
        if (!this.exceptionHandler.containsKey(type)) {
            this.exceptionHandler[type] = mutableMapOf()
        }
        this.exceptionHandler[type]?.put(path, handler)
        return this
    }

    fun handle(context: KoraHttpContext): Any {
        return this.routes[context.path()]?.let {
            it(context)
        } ?: error("Unhandled request for pathing '${context.path()}'")
    }

    fun hasAbortHandler(abortReason: AbortReason<out Throwable>): Boolean {
        return (this.exceptionHandler[abortReason.exception::class]?.size ?: 0) > 0
    }

    fun handleAbort(abortScope: KoraAbortHttpContext, abortReason: AbortReason<out Throwable>): Any {
        return this.exceptionHandler[abortReason.exception::class]?.get(abortScope.path())?.let {
            it(abortScope, abortReason)
        } ?: EndingEarlyException.abort()
    }
}