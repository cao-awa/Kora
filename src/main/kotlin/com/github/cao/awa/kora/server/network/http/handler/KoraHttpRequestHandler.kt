package com.github.cao.awa.kora.server.network.http.handler

import com.github.cao.awa.kora.server.network.handler.KoraRequestHandler
import com.github.cao.awa.kora.server.network.http.context.KoraHttpContext
import com.github.cao.awa.kora.server.network.http.context.abort.KoraAbortHttpContext
import com.github.cao.awa.kora.server.network.exception.abort.UnexpectedBehaviorException
import com.github.cao.awa.kora.server.network.control.abort.reason.AbortReason
import com.github.cao.awa.kora.server.network.http.holder.KoraFullHttpRequestHolder
import com.github.cao.awa.kora.server.network.http.path.exception.HttpPathNotRegisteredException
import io.netty.handler.codec.http.HttpMethod
import kotlin.reflect.KClass

abstract class KoraHttpRequestHandler(val method: HttpMethod) :
    KoraRequestHandler<KoraFullHttpRequestHolder, KoraHttpContext, KoraAbortHttpContext>() {
    private val routes: MutableMap<String, KoraHttpContext.() -> Any> = mutableMapOf()
    private val exceptionHandlers: MutableMap<KClass<out Throwable>, MutableMap<String, KoraAbortHttpContext.(AbortReason<out Throwable>) -> Any>> =
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
        if (!this.exceptionHandlers.containsKey(type)) {
            this.exceptionHandlers[type] = mutableMapOf()
        }
        this.exceptionHandlers[type]?.put(path, handler)
        return this
    }

    override fun hasRoute(path: String): Boolean {
        return this.routes.containsKey(path)
    }

    override fun handle(context: KoraHttpContext): Any {
        return this.routes[context.path()]?.let {
            it(context)
        } ?: pageNotFound(context)
    }

    override fun hasAbortHandler(abortReason: AbortReason<out Throwable>): Boolean {
        return (this.exceptionHandlers[abortReason.exception::class]?.size ?: 0) > 0
    }

    override fun handleAbort(
        abortScope: KoraAbortHttpContext,
        abortReason: AbortReason<out Throwable>,
        responser: (Any) -> Unit
    ): Any {
        val handlers = this.exceptionHandlers[abortReason.exception::class]

        val handler = handlers?.get(abortScope.path())

        if (handler != null) {
            return responser(handler(abortScope, abortReason))
        } else if (handlers == null) {
            UnexpectedBehaviorException.abort()
        } else {
            pageNotFound(abortScope)
        }
    }

    fun pageNotFound(context: KoraHttpContext): Nothing = throw HttpPathNotRegisteredException("Not registered request handler found for pathing '${context.path()}' (404 page not found)")
}