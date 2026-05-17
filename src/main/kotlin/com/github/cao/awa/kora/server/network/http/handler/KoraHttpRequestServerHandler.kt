package com.github.cao.awa.kora.server.network.http.handler

import com.github.cao.awa.kora.server.network.http.context.abort.KoraAbortHttpContext
import kotlin.reflect.KClass

class KoraHttpRequestServerAbortHandler(private val exceptionHandler: MutableMap<KClass<out Throwable>, KoraAbortHttpContext.(Throwable) -> Any>) {
    fun hasHandler(type: KClass<out Throwable>): Boolean {
        return this.exceptionHandler[type] != null
    }

    fun handleAbort(
        abortScope: KoraAbortHttpContext,
        exception: Throwable
    ): Any {
        val handler = this.exceptionHandler[exception::class]

        if (handler != null) {
            return handler(abortScope, exception)
        } else {
            return exception
        }
    }
}