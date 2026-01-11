package com.github.cao.awa.kora.server.network.response

import com.github.cao.awa.cason.codec.encoder.JSONEncoder
import com.github.cao.awa.cason.obj.JSONObject
import com.github.cao.awa.cason.serialize.parser.StrictJSONParser
import com.github.cao.awa.kora.server.network.http.content.type.HttpContentType
import com.github.cao.awa.kora.server.network.http.content.type.HttpContentTypes
import com.github.cao.awa.kora.server.network.http.response.KoraHttpResponses
import com.github.cao.awa.kora.server.network.http.response.KoraHttpResponses.setContentType
import com.github.cao.awa.kora.server.network.http.response.KoraHttpResponses.setJSONHeader
import com.github.cao.awa.kora.server.network.http.response.KoraHttpResponses.setLength
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpVersion
import java.nio.charset.StandardCharsets

class KoraContext(private val context: ChannelHandlerContext, private val msg: FullHttpRequest) {
    var promiseClose: Boolean = false
    var status: HttpResponseStatus = HttpResponseStatus.OK
    var contentType: HttpContentType = HttpContentTypes.PLAIN

    fun response(response: KoraContext.() -> String) {
        val msg: String = response(this)

        this.context.writeAndFlush(
            KoraHttpResponses.createDefaultResponse(
                HttpVersion.HTTP_1_1,
                this.status,
                msg
            ).setContentType(this.contentType)
                .setLength()
        ).also {
            if (this.promiseClose) {
                it.addListener(ChannelFutureListener.CLOSE)
            }
        }
    }

    fun responseJSON(response: KoraContext.() -> JSONObject) {
        val msg: JSONObject = response(this)

        this.contentType = HttpContentTypes.JSON

        response {
            JSONEncoder.encodeJSON(msg)
        }
    }

    fun content(): ByteArray {
        return this.msg.content().let { content ->
            ByteArray(content.readableBytes()).also {
                content.readBytes(it)
            }
        }
    }

    fun stringContent(): String {
        return String(content(), StandardCharsets.UTF_8)
    }

    fun jsonContent(): JSONObject {
        return StrictJSONParser.parseObject(stringContent())
    }
}