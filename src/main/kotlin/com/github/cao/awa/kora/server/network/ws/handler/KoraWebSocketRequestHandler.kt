package com.github.cao.awa.kora.server.network.ws.handler

import com.github.cao.awa.kora.server.network.handler.KoraRequestHandler
import com.github.cao.awa.kora.server.network.exception.abort.EndingEarlyException
import com.github.cao.awa.kora.server.network.control.abort.reason.AbortReason
import com.github.cao.awa.kora.server.network.ws.context.KoraWebSocketContext
import com.github.cao.awa.kora.server.network.ws.context.abort.KoraAbortWebSocketContext
import com.github.cao.awa.kora.server.network.ws.holder.KoraTextWebsocketFrameHolder
import com.github.cao.awa.kora.server.network.ws.phase.KoraWebSocketPhase
import kotlin.reflect.KClass

class KoraWebSocketRequestHandler: KoraRequestHandler<KoraTextWebsocketFrameHolder, KoraWebSocketContext, KoraAbortWebSocketContext>() {
    private val routes: MutableMap<String, MutableMap<KoraWebSocketPhase, KoraWebSocketContext.() -> Any>> = mutableMapOf()
    private val exceptionHandler: MutableMap<KClass<out Throwable>, MutableMap<String, KoraAbortWebSocketContext.(AbortReason<out Throwable>) -> Any>> =
        mutableMapOf()

    fun route(path: String, phase: KoraWebSocketPhase, handler: KoraWebSocketContext.() -> Any): KoraWebSocketRequestHandler {
        if (!this.routes.containsKey(path)) {
            this.routes[path] = mutableMapOf()
        }
        this.routes[path]?.put(phase, handler)
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
            it[context.phase]?.let { handler ->
                handler(context)
            }
        } ?: error("Unhandled request for pathing '${context.path()}'")
    }

    override fun hasRoute(path: String): Boolean {
        // Missing phase.
        return false
    }

    fun hasRoute(path: String, phase: KoraWebSocketPhase): Boolean {
        return this.routes[path]?.get(phase) != null
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