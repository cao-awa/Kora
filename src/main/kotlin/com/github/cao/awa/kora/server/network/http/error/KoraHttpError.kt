package com.github.cao.awa.kora.server.network.http.error

import com.github.cao.awa.cason.obj.JSONObject
import com.github.cao.awa.kora.constant.KoraInformation
import com.github.cao.awa.kora.server.network.http.pipeline.KoraHttpRequestPipeline
import com.github.cao.awa.kora.server.network.http.response.KoraHttpResponses
import io.netty.handler.codec.http.FullHttpResponse
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpVersion

class KoraHttpError(
    val status: HttpResponseStatus,
    val httpVersion: HttpVersion,
    val exception: Throwable,
    val message: String
) {
    fun createResponse(): FullHttpResponse {
        return KoraHttpResponses.createDefaultResponse(
            httpVersion,
            this.status,
            KoraHttpRequestPipeline.instructHttpMetadata(JSONObject {
                "error" set "Server protocol (Kora/${KoraInformation.VERSION}, ${httpVersion.text()}) error: ${status.reasonPhrase()}"
                "internal_error_name" set "${status.reasonPhrase()}"
                "error_message" set message
                array("stacktrace") {
                    +exception.toString()
                    exception.stackTrace.forEach {
                        +" - at $it"
                    }
                }
            }, this.status, this.httpVersion).toString(true, "    ", 0)
        )
    }
}