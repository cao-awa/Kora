package com.github.cao.awa.kora.server.network.http.adapter

import com.github.cao.awa.kora.server.network.http.builder.KoraHttpServerBuilder
import com.github.cao.awa.kora.server.network.http.error.KoraHttpError
import com.github.cao.awa.kora.server.network.http.pipeline.KoraHttpRequestPipeline
import com.github.cao.awa.kora.server.network.http.context.KoraContext
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.HttpVersion

class KoraHttpInboundHandlerAdapter(val pipeline: KoraHttpRequestPipeline) : ChannelInboundHandlerAdapter() {
    constructor(builder: KoraHttpServerBuilder) : this(KoraHttpRequestPipeline()) {
        builder.applyRoute(this)
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        when (msg) {
            is FullHttpRequest -> {
                this.pipeline.handleFull(ctx, KoraContext(msg))
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
        this.pipeline.handleExceptionCaught(ctx, cause)
    }
}