package com.github.cao.awa.kora.server.network.ws.config.decoder

import io.netty.handler.codec.http.websocketx.WebSocketDecoderConfig

data class KoraWebSocketDecoderConfig(
    val maxFramePayloadLength: Int,
    val expectMaskedFrames: Boolean,
    val allowMaskMismatch: Boolean,
    val allowExtensions: Boolean,
    val closeOnProtocolViolation: Boolean,
    val withUTF8Validator: Boolean
) {
    companion object {
        val DEFAULT: KoraWebSocketDecoderConfig = KoraWebSocketDecoderConfig(
            maxFramePayloadLength = 1024 * 512,
            expectMaskedFrames = true,
            allowMaskMismatch = false,
            allowExtensions = false,
            closeOnProtocolViolation = true,
            withUTF8Validator = true
        )
    }

    fun toWebSocketDecoderConfig(): WebSocketDecoderConfig {
        return WebSocketDecoderConfig.newBuilder()
            .maxFramePayloadLength(this.maxFramePayloadLength)
            .expectMaskedFrames(this.expectMaskedFrames)
            .allowMaskMismatch(this.allowMaskMismatch)
            .allowExtensions(this.allowExtensions)
            .closeOnProtocolViolation(this.closeOnProtocolViolation)
            .withUTF8Validator(this.withUTF8Validator)
            .build()
    }
}