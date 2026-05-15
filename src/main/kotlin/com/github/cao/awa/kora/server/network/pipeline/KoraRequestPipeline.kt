package com.github.cao.awa.kora.server.network.pipeline

import com.github.cao.awa.kora.server.network.context.KoraContext
import com.github.cao.awa.kora.server.network.context.abort.KoraAbortContext
import com.github.cao.awa.kora.server.network.handler.KoraRequestHandler
import com.github.cao.awa.kora.server.network.holder.PathByteBufHolder
import com.github.cao.awa.kora.server.network.control.abort.reason.AbortReason
import io.netty.channel.ChannelHandlerContext

abstract class KoraRequestPipeline<B: PathByteBufHolder, C: KoraContext<B, C, A>, A: KoraAbortContext<B>, H: KoraRequestHandler<B, C, A>> {
    fun abortable(
        handlerContext: ChannelHandlerContext,
        koraContext: C,
        handler: H?,
        action: () -> Unit
    ) {
        try {
            // Do program logic.
            action()
        } catch (exception: Throwable) {
            // When exception, reveal all the details by Kora framework.
            try {
                // Inherite current request context, responser need this context.
                val responseScope = koraContext.createInherited()
                // Create abort context, used to response custom logic.
                val abortScope = koraContext.createAbort()
                val reason = exception.message!!
                val abortReason = AbortReason(exception, reason)
                // Handle exception by abort handler with abort scope (inherited by current context).
                if (handler != null && handler.hasAbortHandler(abortReason)) {
                    handler.handleAbort(abortScope, abortReason){
                        response(handlerContext, responseScope, it)
                    }
                } else {
                    // If no handler found, let Kora framework to handle this exception.
                    throw exception
                }
            } catch (unhandledException: Throwable) {
                handleException(
                    unhandledException,
                    handlerContext,
                    koraContext
                )
            }
        }
    }

    abstract fun handleException(exception: Throwable, handlerContext: ChannelHandlerContext, koraContext: C)

    abstract fun response(
        handlerContext: ChannelHandlerContext,
        koraContext: C,
        response: Any
    )
}