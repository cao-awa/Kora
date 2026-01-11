package com.github.cao.awa.kora.server.network.http.response

import com.github.cao.awa.kora.server.network.http.content.type.HttpContentType
import com.github.cao.awa.kora.server.network.http.content.type.HttpContentTypes
import io.netty.buffer.Unpooled
import io.netty.handler.codec.http.DefaultFullHttpResponse
import io.netty.handler.codec.http.FullHttpResponse
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpVersion
import io.netty.util.CharsetUtil

object KoraHttpResponses {
    fun createDefaultResponse(
        httpVersion: HttpVersion,
        status: HttpResponseStatus,
        message: String
    ): FullHttpResponse {
        return DefaultFullHttpResponse(
            httpVersion,
            status,
            Unpooled.copiedBuffer(
                message,
                CharsetUtil.UTF_8
            )
        ).setPlainHeader()
    }

    fun FullHttpResponse.setPlainHeader(): FullHttpResponse {
        setContentType(HttpContentTypes.PLAIN)
        return this
    }

    fun FullHttpResponse.setJSONHeader(): FullHttpResponse {
        setContentType(HttpContentTypes.JSON)
        return this
    }

    fun FullHttpResponse.setContentType(contentType: HttpContentType): FullHttpResponse {
        headers().set(HttpHeaderNames.CONTENT_TYPE, contentType.name)
        return this
    }

    fun FullHttpResponse.setLength(): FullHttpResponse {
        headers().set(HttpHeaderNames.CONTENT_LENGTH, content().readableBytes())
        return this
    }
}