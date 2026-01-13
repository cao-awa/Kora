package com.github.cao.awa.kora.server.network.http.pipeline

import com.github.cao.awa.cason.codec.encoder.JSONEncoder
import com.github.cao.awa.cason.obj.JSONObject
import com.github.cao.awa.kora.server.network.http.KoraHttpServer
import com.github.cao.awa.kora.server.network.http.content.type.HttpContentTypes
import com.github.cao.awa.kora.server.network.http.context.KoraContext
import com.github.cao.awa.kora.server.network.http.context.abort.AbortKoraContext
import com.github.cao.awa.kora.server.network.http.control.abort.reason.AbortReason
import com.github.cao.awa.kora.server.network.http.error.KoraHttpError
import com.github.cao.awa.kora.server.network.http.exception.NotSupportedMethodException
import com.github.cao.awa.kora.server.network.http.handler.KoraHttpRequestHandler
import com.github.cao.awa.kora.server.network.http.handler.get.KoraHttpGetHandler
import com.github.cao.awa.kora.server.network.http.handler.post.KoraHttpPostHandler
import com.github.cao.awa.kora.server.network.http.metadata.HttpResponseMetadata
import com.github.cao.awa.kora.server.network.http.response.KoraHttpResponses
import com.github.cao.awa.kora.server.network.http.response.KoraHttpResponses.setContentType
import com.github.cao.awa.kora.server.network.http.response.KoraHttpResponses.setLength
import com.github.cao.awa.kora.server.network.response.content.NoContentResponse
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.HttpMethod
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpVersion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class KoraHttpRequestPipeline {
    companion object {
        fun instructHttpMetadata(json: JSONObject, koraContext: KoraContext): JSONObject {
            json.instruct {
                if (KoraHttpServer.instructHttpMetadata) {
                    nested("http_meta") {
                        HttpResponseMetadata(
                            if (KoraHttpServer.instructHttpStatusCode) {
                                koraContext.status().code()
                            } else null,
                            if (KoraHttpServer.instructHttpVersionCode) {
                                koraContext.protocolVersion().text()
                            } else null
                        )
                    }
                }
            }

            return json
        }

        fun instructHttpMetadata(
            json: JSONObject,
            status: HttpResponseStatus,
            protocolVersion: HttpVersion
        ): JSONObject {
            json.instruct {
                if (KoraHttpServer.instructHttpMetadata) {
                    nested("http_meta") {
                        HttpResponseMetadata(
                            if (KoraHttpServer.instructHttpStatusCode) {
                                status.code()
                            } else null,
                            if (KoraHttpServer.instructHttpVersionCode) {
                                protocolVersion.text()
                            } else null
                        )
                    }
                }
            }

            return json
        }
    }

    private val handlers: Map<HttpMethod, KoraHttpRequestHandler> = buildMap {
        put(HttpMethod.GET, KoraHttpGetHandler())
        put(HttpMethod.POST, KoraHttpPostHandler())
    }
    private val executionScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun getHandler(method: HttpMethod): KoraHttpRequestHandler? = this.handlers[method]

    fun handleFull(handlerContext: ChannelHandlerContext, koraContext: KoraContext) {
        // CoroutineScope.
        this.executionScope.launch {
            abortable(handlerContext, koraContext) {
                response(
                    handlerContext = handlerContext,
                    koraContext = koraContext,
                    response = run {
                        val handler: KoraHttpRequestHandler? = getHandler(koraContext.method())
                        handler?.handle(koraContext) ?: throw NotSupportedMethodException(koraContext.method())
                    }
                )
            }
        }
    }

    private fun abortable(handlerContext: ChannelHandlerContext, koraContext: KoraContext, action: () -> Unit) {
        try {
            action()
        } catch (exception: RuntimeException) {
            val abortScope = AbortKoraContext(koraContext)
            val reason = exception.message ?: "Control stream lifecycle aborting"
            val abortReason = AbortReason(exception, reason)
            val handler: KoraHttpRequestHandler? = getHandler(koraContext.method())
            try {
                if (handler != null) {
                    response(handlerContext, abortScope, handler.handleAbort(abortScope, abortReason))
                } else {
                    error("Unprocessable handle in path ${koraContext.path()}")
                }
            } catch (exception: Exception) {
                val endOfLifecycleScope = abortScope.createInherited()
                response(
                    handlerContext,
                    endOfLifecycleScope,
                    NoContentResponse
                )
            }
        }
    }

    fun handleExceptionCaught(handlerContext: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        // Response an error message.
        handlerContext.writeAndFlush(
            KoraHttpError.INTERNAL_SERVER_ERROR(HttpVersion.HTTP_1_0)
        ).addListener(ChannelFutureListener.CLOSE)
    }

    private fun response(handlerContext: ChannelHandlerContext, koraContext: KoraContext, response: Any) {
        when (response) {
            is JSONObject -> {
                responseJSON(handlerContext, koraContext) {
                    response
                }
            }

            is NoContentResponse -> {
                response(handlerContext, koraContext) {
                    // Force be no content status when response is no body response.
                    koraContext.withStatus(HttpResponseStatus.NO_CONTENT)

                    ""
                }
            }

            else -> {
                responseJSON(handlerContext, koraContext) {
                    JSONEncoder.encode(response)
                }
            }
        }
    }

    private fun response(
        handlerContext: ChannelHandlerContext,
        koraContext: KoraContext,
        response: KoraContext.() -> String
    ) {
        val msg: String = response(koraContext)

        handlerContext.writeAndFlush(
            KoraHttpResponses.createDefaultResponse(
                koraContext.protocolVersion(),
                koraContext.status(),
                msg
            ).setContentType(koraContext.contentType())
                .setLength()
        ).also {
            if (koraContext.promiseClose) {
                it.addListener(ChannelFutureListener.CLOSE)
            }
        }
    }

    private fun responseJSON(
        handlerContext: ChannelHandlerContext,
        koraContext: KoraContext,
        responser: KoraContext.() -> JSONObject
    ) {
        val sendingContext = koraContext.createInherited()

        val msg: JSONObject = instructHttpMetadata(responser(sendingContext), sendingContext)

        sendingContext.withContentType(HttpContentTypes.JSON)

        response(handlerContext, sendingContext) {
            JSONEncoder.renderJSON(msg)
        }
    }
}