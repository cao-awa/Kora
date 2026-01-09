package com.github.cao.awa.kora.server.network.http.error

import com.github.cao.awa.kora.server.KoraInformation
import io.netty.buffer.Unpooled
import io.netty.handler.codec.http.DefaultFullHttpResponse
import io.netty.handler.codec.http.FullHttpResponse
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpVersion
import io.netty.util.CharsetUtil
import java.net.http.HttpResponse

object KoraHttpError {
    val FAILURE_NOT_FULL: (HttpVersion) -> FullHttpResponse = { httpVersion ->
        setPlainHeader(
            createDefaultResponse(
                httpVersion,
                HttpResponseStatus.BAD_REQUEST,
                """
Server protocol (Kora/${KoraInformation.VERSION}, ${httpVersion}) error: Bad request
Internal error name: Not full"""
            )
        )
    }

    val INTERNAL_SERVER_ERROR: (HttpVersion) -> FullHttpResponse = { httpProtocolVersion ->
        setPlainHeader(
            createDefaultResponse(
                httpProtocolVersion,
                HttpResponseStatus.INTERNAL_SERVER_ERROR,
                """
Server protocol (Kora/${KoraInformation.VERSION}, ${httpProtocolVersion}) error: Internal server error
Internal error name: Internal server error"""
            )
        )
    }

    private fun createDefaultResponse(
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
        )
    }

    private fun setPlainHeader(response: FullHttpResponse): FullHttpResponse {
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain")
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes())
        return response
    }
}