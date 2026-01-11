package com.github.cao.awa.kora.server.network.http.error

import com.github.cao.awa.kora.constant.KoraInformation
import com.github.cao.awa.kora.server.network.http.response.KoraHttpResponses
import io.netty.handler.codec.http.FullHttpResponse
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpVersion

object KoraHttpError {
    val FAILURE_NOT_FULL: (HttpVersion) -> FullHttpResponse = { httpVersion ->
        KoraHttpResponses.createDefaultResponse(
            httpVersion,
            HttpResponseStatus.BAD_REQUEST,
            """
Server protocol (Kora/${KoraInformation.VERSION}, ${httpVersion}) error: Bad request
Internal error name: Not full"""
        )
    }

    val INTERNAL_SERVER_ERROR: (HttpVersion) -> FullHttpResponse = { httpProtocolVersion ->
        KoraHttpResponses.createDefaultResponse(
            httpProtocolVersion,
            HttpResponseStatus.INTERNAL_SERVER_ERROR,
            """
Server protocol (Kora/${KoraInformation.VERSION}, ${httpProtocolVersion}) error: Internal server error
Internal error name: Internal server error"""
        )
    }
}