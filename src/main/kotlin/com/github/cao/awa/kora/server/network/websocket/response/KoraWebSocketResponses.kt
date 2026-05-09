package com.github.cao.awa.kora.server.network.websocket.response

import io.netty.handler.codec.http.websocketx.TextWebSocketFrame

object KoraWebSocketResponses {
    fun createDefaultResponse(text: String): TextWebSocketFrame {
        return TextWebSocketFrame(text)
    }
}