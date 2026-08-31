package com.github.cao.awa.kalmia.server.network.websocket.client.adapter

import com.github.cao.awa.kalmia.server.network.websocket.client.KalmiaWebSocketClient
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.websocketx.WebSocketFrame

class KalmiaWebSocketClientAdapter(
    private val client: KalmiaWebSocketClient
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