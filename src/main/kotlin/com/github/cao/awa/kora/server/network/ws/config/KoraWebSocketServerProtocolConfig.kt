package com.github.cao.awa.kora.server.network.ws.config

import com.github.cao.awa.kora.server.network.ws.config.decoder.KoraWebSocketDecoderConfig
import io.netty.handler.codec.http.websocketx.WebSocketCloseStatus

data class KoraWebSocketServerProtocolConfig(
    val subprotocols: String?,
    val checkStartsWith: Boolean,
    val handshakeTimeoutMillis: Long,
    val forceCloseTimeoutMillis: Long,
    val handleCloseFrames: Boolean,
    val sendCloseFrame: WebSocketCloseStatus,
    val dropPongFrames: Boolean,
    val decoderConfig: KoraWebSocketDecoderConfig
)