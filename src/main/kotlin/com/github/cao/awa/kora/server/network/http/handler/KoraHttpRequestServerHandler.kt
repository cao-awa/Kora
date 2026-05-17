package com.github.cao.awa.kora.server.network.http.handler

import com.github.cao.awa.kora.server.network.control.abort.reason.AbortReason
import com.github.cao.awa.kora.server.network.http.context.abort.KoraAbortHttpContext
import kotlin.reflect.KClass

class KoraHttpRequestServerAbortHandler(private val exceptionHandler: MutableMap<KClass<out Throwable>, KoraAbortHttpContext.(AbortReason<out Throwable>) -> Any>) {
    fun hasHandler(type: KClass<out Throwable>): Boolean {
        return this.exceptionHandler[type] != null
    }

    fun handleAbort(
        abortScope: KoraAbortHttpContext,
        abortReason: AbortReason<out Throwable>
    ): Any {
        val handler = this.exceptionHandler[abortReason.exception::class]

        if (handler != null) {
            return handler(abortScope, abortReason)
        } else {
            return abortReason.exception
        }
    }
}