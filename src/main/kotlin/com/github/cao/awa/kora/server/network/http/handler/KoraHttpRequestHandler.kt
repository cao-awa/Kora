package com.github.cao.awa.kora.server.network.http.handler

import com.github.cao.awa.kora.server.network.http.context.KoraHttpContext
import com.github.cao.awa.kora.server.network.http.context.abort.KoraAbortHttpContext
import com.github.cao.awa.kora.server.network.http.exception.abort.EndingEarlyException
import com.github.cao.awa.kora.server.network.http.control.abort.reason.AbortReason
import io.netty.handler.codec.http.HttpMethod
import kotlin.reflect.KClass

abstract class KoraHttpRequestHandler(val method: HttpMethod) {
    private val routes: MutableMap<String, KoraHttpContext.() -> Any> = mutableMapOf()
    private val exceptionHandler: MutableMap<KClass<out Exception>, MutableMap<String, KoraAbortHttpContext.(AbortReason<out Exception>) -> Any>> =
        mutableMapOf()

    fun route(path: String, handler: KoraHttpContext.() -> Any): KoraHttpRequestHandler {
        this.routes[path] = handler
        return this
    }

    fun routeExceptionHandler(
        path: String,
        type: KClass<out Exception>,
        handler: KoraAbortHttpContext.(AbortReason<out Exception>) -> Any
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
        } ?: EndingEarlyException.abort()
    }

    fun handleAbort(abortScope: KoraAbortHttpContext, abortReason: AbortReason<out Exception>): Any {
        return this.exceptionHandler[abortReason.exception::class]?.get(abortScope.path())?.let {
            it(abortScope, abortReason)
        } ?: EndingEarlyException.abort()
    }
}