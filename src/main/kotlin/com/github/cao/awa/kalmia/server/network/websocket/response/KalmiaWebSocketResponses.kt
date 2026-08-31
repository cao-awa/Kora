package com.github.cao.awa.kalmia.server.network.websocket.response

import io.netty.handler.codec.http.websocketx.TextWebSocketFrame

object KalmiaWebSocketResponses {
    fun createDefaultResponse(text: String): TextWebSocketFrame {
        return TextWebSocketFrame(text)
    }
}