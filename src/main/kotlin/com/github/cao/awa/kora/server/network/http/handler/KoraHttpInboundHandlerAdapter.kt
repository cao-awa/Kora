package com.github.cao.awa.kora.server.network.http.handler

import com.github.cao.awa.kora.server.network.http.builder.KoraHttpServerBuilder
import com.github.cao.awa.kora.server.network.http.error.KoraHttpError
import com.github.cao.awa.kora.server.network.response.KoraContext
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.http.*


class KoraHttpInboundHandlerAdapter(val handler: KoraHttpRequestHandler) : ChannelInboundHandlerAdapter() {
    constructor(builder: KoraHttpServerBuilder) : this(KoraHttpRequestHandler()) {
        builder.applyRoute(this)
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        when (msg) {
            is FullHttpRequest -> {
                this.handler.handleFull(ctx, KoraContext(msg))
            }
            is HttpRequest -> {
                ctx.writeAndFlush(
                    KoraHttpError.FAILURE_NOT_FULL(msg.protocolVersion())
                ).addListener(ChannelFutureListener.CLOSE)
            }
            else -> {
                ctx.writeAndFlush(
                    KoraHttpError.INTERNAL_SERVER_ERROR(HttpVersion.HTTP_1_0)
                ).addListener(ChannelFutureListener.CLOSE)
            }
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        // Response an error message.
        ctx.writeAndFlush(
            KoraHttpError.INTERNAL_SERVER_ERROR(HttpVersion.HTTP_1_0)
        ).addListener(ChannelFutureListener.CLOSE)
    }
}
