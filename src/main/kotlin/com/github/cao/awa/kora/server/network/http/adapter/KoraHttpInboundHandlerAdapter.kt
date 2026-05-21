package com.github.cao.awa.kora.server.network.http.adapter

import com.github.cao.awa.kora.server.network.http.builder.KoraHttpServerBuilder
import com.github.cao.awa.kora.server.network.http.pipeline.KoraHttpRequestPipeline
import com.github.cao.awa.kora.server.network.http.context.KoraHttpContext
import com.github.cao.awa.kora.server.network.http.handler.KoraHttpRequestServerAbortHandler
import com.github.cao.awa.kora.server.network.http.holder.KoraFullHttpRequestHolder
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.http.FullHttpRequest

@ChannelHandler.Sharable
class KoraHttpInboundHandlerAdapter(val pipeline: KoraHttpRequestPipeline) : ChannelInboundHandlerAdapter() {
    constructor(builder: KoraHttpServerBuilder) : this(KoraHttpRequestPipeline(KoraHttpRequestServerAbortHandler(builder.abortHandlers))) {
        builder.applyRoute(this)
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        when (msg) {
            is FullHttpRequest -> {
                // Handle full http request,
                this.pipeline.handleFull(ctx, KoraHttpContext(KoraFullHttpRequestHolder(msg)))
            }

            else -> {
                // TODO: handle errors that got not full http request.
            }
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        this.pipeline.handleExceptionCaught(ctx, cause)
    }
}