package com.github.cao.awa.kora.server.network.pipeline

import com.github.cao.awa.kora.server.network.context.KoraContext
import com.github.cao.awa.kora.server.network.context.abort.KoraAbortContext
import com.github.cao.awa.kora.server.network.handler.KoraRequestHandler
import com.github.cao.awa.kora.server.network.holder.PathByteBufHolder
import com.github.cao.awa.kora.server.network.control.abort.reason.AbortReason
import com.github.cao.awa.kora.server.network.http.error.KoraHttpError
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.HttpVersion

abstract class KoraRequestPipeline<B: PathByteBufHolder, C: KoraContext<B, C, A>, A: KoraAbortContext<B>, H: KoraRequestHandler<B, C, A>> {
    open fun abortable(
        handlerContext: ChannelHandlerContext,
        koraContext: C,
        handler: H?,
        action: () -> Unit
    ) {
        try {
            action()
        } catch (exception: Throwable) {
            try {
                val responseScope = koraContext.createInherited()
                val abortScope = koraContext.createAbort()
                val reason = exception.message!!
                val abortReason = AbortReason(exception, reason)
                if (handler != null && handler.hasAbortHandler(abortReason)) {
                    response(handlerContext, responseScope, handler.handleAbort(abortScope, abortReason))
                } else {
                    throw exception
                }
            } catch (unhandledException: Throwable) {
                // Response an error message.
                handlerContext.writeAndFlush(
                    KoraHttpError.INTERNAL_SERVER_ERROR(HttpVersion.HTTP_1_0, unhandledException)
                ).addListener(ChannelFutureListener.CLOSE)
            }
        }
    }

    abstract fun response(
        handlerContext: ChannelHandlerContext,
        koraContext: C,
        response: Any
    )
}