package com.github.cao.awa.kora.server.network.http.error

import com.github.cao.awa.cason.obj.JSONObject
import com.github.cao.awa.kora.constant.KoraInformation
import com.github.cao.awa.kora.server.network.KoraNetworkConfig
import com.github.cao.awa.kora.server.network.http.context.KoraHttpContext
import com.github.cao.awa.kora.server.network.http.pipeline.KoraHttpRequestPipeline
import com.github.cao.awa.kora.server.network.http.response.KoraHttpResponses
import io.netty.handler.codec.http.FullHttpResponse
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpVersion

class KoraHttpError(
    val status: HttpResponseStatus,
    val httpVersion: HttpVersion,
    val exception: Throwable,
    val message: String,
    val requestPath: String,
    val context: KoraHttpContext?
) {
    companion object {
        private fun fillStacktrace(json: JSONObject, exception: Throwable) {
            json.instruct {
                arr("stacktrace") {
                    +exception.toString()
                    exception.stackTrace.forEach {
                        +" - at $it"
                    }
                }
            }
        }
    }

    fun createResponse(): FullHttpResponse {
        return KoraHttpResponses.createDefaultResponse(
            httpVersion,
            this.status,
            let {
                val json = JSONObject {
                    "error" set "Server protocol error (Kora/${KoraInformation.VERSION}, ${httpVersion.text()}): ${status.reasonPhrase()}"
                    "internal_error_name" set "${status.reasonPhrase()}"
                    "error_message" set message
                    if (KoraNetworkConfig.responseFillStacktrace) {
                        fillStacktrace(this, exception)
                    } else {
                        val detailsMessage = exception.message
                        if (detailsMessage != null) {
                            "error_details_message" set detailsMessage
                        }
                    }
                }
                let {
                    if (this.context == null) {
                        KoraHttpRequestPipeline.instructHttpMetadata(
                            json,
                            this.status,
                            this.httpVersion,
                            this.requestPath
                        )
                    } else {
                        KoraHttpRequestPipeline.instructHttpMetadata(
                            json,
                            this.context
                        )
                    }
                }.toString(true, "    ", 0)
            }
        )
    }
}