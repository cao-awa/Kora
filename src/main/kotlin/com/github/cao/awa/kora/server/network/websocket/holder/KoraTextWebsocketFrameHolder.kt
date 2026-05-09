package com.github.cao.awa.kora.server.network.websocket.holder

import com.github.cao.awa.kora.server.network.holder.PathByteBufHolder
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame

class KoraTextWebsocketFrameHolder(val msg: TextWebSocketFrame, uri: String): PathByteBufHolder(msg, uri) {
    fun text(): String = this.msg.text()
}