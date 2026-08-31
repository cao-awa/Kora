package com.github.cao.awa.kora.server.network.websocket.client.adapter

import com.github.cao.awa.kora.server.network.websocket.client.KoraWebSocketClient
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.websocketx.WebSocketFrame

class KoraWebSocketClientAdapter(
    private val client: KoraWebSocketClient
) : SimpleChannelInboundHandler<WebSocketFrame>() {
    private lateinit var currentContext: ChannelHandlerContext

    @Override
    override fun channelActive(ctx: ChannelHandlerContext?) {
        this.client.setAdapter(this)
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: WebSocketFrame) {
        this.currentContext = ctx
        this.client.fireMessage(msg)
    }

    fun sendMessage(msg: Any) {
        this.currentContext.writeAndFlush(msg)
    }
}