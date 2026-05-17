package com.github.cao.awa.kora.server.network.http.handler

import com.github.cao.awa.kora.server.network.handler.KoraRequestHandler
import com.github.cao.awa.kora.server.network.http.context.KoraHttpContext
import com.github.cao.awa.kora.server.network.http.context.abort.KoraAbortHttpContext
import com.github.cao.awa.kora.server.network.http.holder.KoraFullHttpRequestHolder
import com.github.cao.awa.kora.server.network.http.exception.path.HttpPathNotRegisteredException
import com.github.cao.awa.kora.server.network.http.url.KoraPlaceholderURL
import com.github.cao.awa.kora.server.network.http.url.urlParameterRoute
import io.netty.handler.codec.http.HttpMethod
import kotlin.reflect.KClass

abstract class KoraHttpRequestHandler(val method: HttpMethod) :
    KoraRequestHandler<KoraFullHttpRequestHolder, KoraHttpContext, KoraAbortHttpContext>() {
    private val routes: MutableMap<KoraPlaceholderURL, KoraHttpContext.() -> Any> = HashMap()
    private val noPlaceholderRoutes: MutableMap<String, KoraHttpContext.() -> Any> = HashMap()
    private val exceptionHandlers: MutableMap<KClass<out Throwable>, MutableMap<KoraPlaceholderURL, KoraAbortHttpContext.(Throwable) -> Any>> =
        mutableMapOf()

    fun route(path: String, handler: KoraHttpContext.() -> Any): KoraHttpRequestHandler {
        val url = path.urlParameterRoute()
        if (url.hasPlaceholder()) {
            this.routes[url] = handler
        } else {
            this.noPlaceholderRoutes[path] = handler
        }
        return this
    }

    fun routeExceptionHandler(
        path: String,
        type: KClass<out Throwable>,
        handler: KoraAbortHttpContext.(Throwable) -> Any
    ): KoraHttpRequestHandler {
        if (!this.exceptionHandlers.containsKey(type)) {
            this.exceptionHandlers[type] = mutableMapOf()
        }
        this.exceptionHandlers[type]?.put(path.urlParameterRoute(), handler)
        return this
    }

    override fun hasRoute(path: String): Boolean {
        return this.noPlaceholderRoutes.containsKey(path) || this.routes.containsKey(path.urlParameterRoute())
    }

    override fun handle(context: KoraHttpContext): Any {
        val specificRoute = this.noPlaceholderRoutes[context.path()]

        // Pre-fetch to optimization route search speed.
        if (specificRoute != null) {
            return specificRoute(context)
        }

        var matchPlaceholderHandler: (KoraHttpContext.() -> Any)? = null
        var placeholderURL: KoraPlaceholderURL? = null

        // Search route.
        val url = context.path().urlParameterRoute()
        for ((routeUrl, route) in this.routes) {
            if (routeUrl == url && !routeUrl.hasPlaceholder()) {
                return route(context)
            } else if (url.matchPlaceholder(routeUrl)) {
                matchPlaceholderHandler = route
                placeholderURL = routeUrl
                break
            }
        }

        // If route has placeholder, create context with placeholder.
        if (matchPlaceholderHandler != null && placeholderURL != null) {
            return matchPlaceholderHandler(context.withPlaceholder(placeholderURL))
        }

        // If not found a specific route or placeholder route, throws 404 NOT_FOUND error.
        routeNotFound(context)
    }

    override fun hasAbortHandler(exception: Throwable): Boolean {
        return (this.exceptionHandlers[exception::class]?.size ?: 0) > 0
    }

    override fun handleAbort(
        abortScope: KoraAbortHttpContext,
        exception: Throwable,
        responser: (Any) -> Unit
    ): Any {
        val handlers = this.exceptionHandlers[exception::class]

        val handler = handlers?.get(abortScope.path().urlParameterRoute())

        if (handler != null) {
            return responser(handler(abortScope, exception))
        } else if (handlers == null) {
            throw exception
        } else {
            routeNotFound(abortScope)
        }
    }

    fun routeNotFound(context: KoraHttpContext): Nothing = HttpPathNotRegisteredException.notFound(context.path())
}