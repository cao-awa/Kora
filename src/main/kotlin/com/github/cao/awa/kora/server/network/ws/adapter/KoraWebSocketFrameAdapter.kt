package com.github.cao.awa.kora.server.network.ws.adapter

import com.github.cao.awa.kora.server.network.http.adapter.KoraHttpInboundHandlerAdapter
import com.github.cao.awa.kora.server.network.http.builder.KoraHttpServerBuilder
import com.github.cao.awa.kora.server.network.http.pipeline.KoraHttpRequestPipeline
import com.github.cao.awa.kora.server.network.ws.builder.KoraWebsocketServerBuilder
import com.github.cao.awa.kora.server.network.ws.context.KoraWebSocketContext
import com.github.cao.awa.kora.server.network.ws.holder.KoraTextWebsocketFrameHolder
import com.github.cao.awa.kora.server.network.ws.pipeline.KoraWebSocketRequestPipeline
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame

class KoraWebSocketFrameAdapter(val pipeline: KoraWebSocketRequestPipeline) : ChannelInboundHandlerAdapter() {
    constructor(builder: KoraWebsocketServerBuilder) : this(KoraWebSocketRequestPipeline()) {
        builder.applyRoute(this)
    }

    override fun channelRead(
        context: ChannelHandlerContext,
        message: Any
    ) {
        if (message is KoraTextWebsocketFrameHolder) {
            val text = message.text()
            println("Received: $text")

            // 回写消息
            context.channel().writeAndFlush(
                TextWebSocketFrame("Echo: $text")
            )

            this.pipeline.handle(context, KoraWebSocketContext(message))
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        this.pipeline.handleExceptionCaught(ctx, cause)
    }
}
