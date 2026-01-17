package com.github.cao.awa.kora.server.network.http.error

import com.github.cao.awa.cason.obj.JSONObject
import com.github.cao.awa.kora.constant.KoraInformation
import com.github.cao.awa.kora.server.network.http.pipeline.KoraHttpRequestPipeline
import com.github.cao.awa.kora.server.network.http.response.KoraHttpResponses
import io.netty.handler.codec.http.FullHttpResponse
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpVersion

object KoraHttpError {
    val FAILURE_NOT_FULL: (HttpVersion) -> FullHttpResponse = { protocolVersion ->
        KoraHttpResponses.createDefaultResponse(
            protocolVersion,
            HttpResponseStatus.BAD_REQUEST,
            KoraHttpRequestPipeline.instructHttpMetadata(JSONObject {
                "error" set "Server protocol (Kora/${KoraInformation.VERSION}, ${protocolVersion.text()}) error: Bad request"
                "internal_error_name" set "Request is not full"
            }, HttpResponseStatus.BAD_REQUEST, protocolVersion).toString(true, "    ", 0)
        )
    }

    val BAD_REQUEST: (HttpVersion) -> FullHttpResponse = { protocolVersion ->
        KoraHttpResponses.createDefaultResponse(
            protocolVersion,
            HttpResponseStatus.BAD_REQUEST,
            KoraHttpRequestPipeline.instructHttpMetadata(JSONObject {
                "error" set "Server protocol (Kora/${KoraInformation.VERSION}, ${protocolVersion.text()}) error: Bad request"
                "internal_error_name" set "Request is not full"
            }, HttpResponseStatus.BAD_REQUEST, protocolVersion).toString(true, "    ", 0)
        )
    }

    val INTERNAL_SERVER_ERROR: (HttpVersion, Throwable) -> FullHttpResponse = { protocolVersion, exception ->
        KoraHttpResponses.createDefaultResponse(
            protocolVersion,
            HttpResponseStatus.INTERNAL_SERVER_ERROR,
            KoraHttpRequestPipeline.instructHttpMetadata(JSONObject {
                "message" set "Server protocol (Kora/${KoraInformation.VERSION}, ${protocolVersion.text()}) error: Internal server error"
                "internal_error_name" set "Internal server error"
                array("stacktrace") {
                    + exception.toString()
                    exception.stackTrace.forEach {
                        + " - at $it"
                    }
                }
            }, HttpResponseStatus.INTERNAL_SERVER_ERROR, protocolVersion).toString(true, "    ", 0)
        )
    }
}