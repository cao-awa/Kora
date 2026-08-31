package com.github.cao.awa.kalmia.server.network.websocket.holder

import com.github.cao.awa.kalmia.server.network.holder.PathByteBufHolder
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame

class KalmiaTextWebsocketFrameHolder(val msg: TextWebSocketFrame, uri: String): PathByteBufHolder(msg, uri) {
    fun text(): String = this.msg.text()
}