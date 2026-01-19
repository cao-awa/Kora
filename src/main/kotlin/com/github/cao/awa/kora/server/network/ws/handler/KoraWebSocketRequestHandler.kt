package com.github.cao.awa.kora.server.network.ws.handler

import com.github.cao.awa.kora.server.network.handler.KoraRequestHandler
import com.github.cao.awa.kora.server.network.http.context.KoraHttpContext
import com.github.cao.awa.kora.server.network.http.context.abort.KoraAbortHttpContext
import com.github.cao.awa.kora.server.network.http.exception.abort.EndingEarlyException
import com.github.cao.awa.kora.server.network.http.control.abort.reason.AbortReason
import com.github.cao.awa.kora.server.network.http.holder.KoraFullHttpRequestHolder
import com.github.cao.awa.kora.server.network.ws.context.KoraWebSocketContext
import com.github.cao.awa.kora.server.network.ws.context.abort.KoraAbortWebSocketContext
import com.github.cao.awa.kora.server.network.ws.holder.KoraTextWebsocketFrameHolder
import io.netty.handler.codec.http.HttpMethod
import kotlin.reflect.KClass

class KoraWebSocketRequestHandler: KoraRequestHandler<KoraTextWebsocketFrameHolder, KoraWebSocketContext, KoraAbortWebSocketContext>() {
    private val routes: MutableMap<String, MutableList<KoraWebSocketContext.() -> Any>> = mutableMapOf()
    private val exceptionHandler: MutableMap<KClass<out Throwable>, MutableMap<String, KoraAbortWebSocketContext.(AbortReason<out Throwable>) -> Any>> =
        mutableMapOf()

    fun route(path: String, handler: KoraWebSocketContext.() -> Any): KoraWebSocketRequestHandler {
        if (!this.routes.containsKey(path)) {
            this.routes[path] = mutableListOf()
        }
        this.routes[path]?.add(handler)
        return this
    }

    fun routeExceptionHandler(
        path: String,
        type: KClass<out Throwable>,
        handler: KoraAbortWebSocketContext.(AbortReason<out Throwable>) -> Any
    ): KoraWebSocketRequestHandler {
        if (!this.exceptionHandler.containsKey(type)) {
            this.exceptionHandler[type] = mutableMapOf()
        }
        this.exceptionHandler[type]?.put(path, handler)
        return this
    }

    override fun handle(context: KoraWebSocketContext): Any {
        return this.routes[context.path()]?.let {
            for (handler in it) {
                handler(context)
            }
        } ?: error("Unhandled request for pathing '${context.path()}'")
    }

    override fun hasRoute(path: String): Boolean {
        return (this.routes[path]?.size ?: 0) > 0
    }

    override fun hasAbortHandler(abortReason: AbortReason<out Throwable>): Boolean {
        return (this.exceptionHandler[abortReason.exception::class]?.size ?: 0) > 0
    }

    override fun handleAbort(abortScope: KoraAbortWebSocketContext, abortReason: AbortReason<out Throwable>): Any {
        return this.exceptionHandler[abortReason.exception::class]?.get(abortScope.path())?.let {
            it(abortScope, abortReason)
        } ?: EndingEarlyException.abort()
    }
}