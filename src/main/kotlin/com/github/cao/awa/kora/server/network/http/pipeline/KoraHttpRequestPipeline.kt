package com.github.cao.awa.kora.server.network.http.pipeline

import com.github.cao.awa.cason.codec.encoder.JSONEncoder
import com.github.cao.awa.cason.obj.JSONObject
import com.github.cao.awa.kora.server.network.control.abort.reason.AbortReason
import com.github.cao.awa.kora.server.network.http.KoraHttpServer
import com.github.cao.awa.kora.server.network.http.content.type.HttpContentTypes
import com.github.cao.awa.kora.server.network.http.context.KoraHttpContext
import com.github.cao.awa.kora.server.network.http.context.abort.KoraAbortHttpContext
import com.github.cao.awa.kora.server.network.http.error.KoraHttpErrors
import com.github.cao.awa.kora.server.network.http.exception.method.NotSupportedHttpMethodException
import com.github.cao.awa.kora.server.network.http.handler.KoraHttpRequestHandler
import com.github.cao.awa.kora.server.network.http.handler.get.KoraHttpGetHandler
import com.github.cao.awa.kora.server.network.http.handler.post.KoraHttpPostHandler
import com.github.cao.awa.kora.server.network.http.holder.KoraFullHttpRequestHolder
import com.github.cao.awa.kora.server.network.http.metadata.HttpResponseMetadata
import com.github.cao.awa.kora.server.network.http.path.exception.HttpPathNotRegisteredException
import com.github.cao.awa.kora.server.network.http.response.KoraHttpResponses
import com.github.cao.awa.kora.server.network.http.response.KoraHttpResponses.setContentType
import com.github.cao.awa.kora.server.network.http.response.KoraHttpResponses.setLength
import com.github.cao.awa.kora.server.network.http.response.content.NoContentResponse
import com.github.cao.awa.kora.server.network.pipeline.KoraRequestPipeline
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.FullHttpResponse
import io.netty.handler.codec.http.HttpMethod
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpVersion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.github.cao.awa.com.github.cao.awa.capertml.html.HTMLElement

class KoraHttpRequestPipeline :
    KoraRequestPipeline<KoraFullHttpRequestHolder, KoraHttpContext, KoraAbortHttpContext, KoraHttpRequestHandler>() {
    companion object {
        fun instructHttpMetadata(json: JSONObject, koraContext: KoraHttpContext): JSONObject {
            json.instruct {
                if (KoraHttpServer.instructHttpMetadata) {
                    nested("http_meta") {
                        HttpResponseMetadata(
                            if (KoraHttpServer.instructHttpStatusCode) {
                                koraContext.status().code()
                            } else null, if (KoraHttpServer.instructHttpVersionCode) {
                                koraContext.protocolVersion().text()
                            } else null
                        )
                    }
                }
            }

            return json
        }

        fun instructHttpMetadata(
            json: JSONObject, status: HttpResponseStatus, protocolVersion: HttpVersion
        ): JSONObject {
            json.instruct {
                if (KoraHttpServer.instructHttpMetadata) {
                    nested("http_meta") {
                        HttpResponseMetadata(
                            if (KoraHttpServer.instructHttpStatusCode) {
                                status.code()
                            } else null, if (KoraHttpServer.instructHttpVersionCode) {
                                protocolVersion.text()
                            } else null
                        )
                    }
                }
            }

            return json
        }
    }

    private val handlers: Map<HttpMethod, KoraHttpRequestHandler> =
        HashMap<HttpMethod, KoraHttpRequestHandler>().apply {
            put(HttpMethod.GET, KoraHttpGetHandler())
            put(HttpMethod.POST, KoraHttpPostHandler())
        }
    private val executionScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun getHandler(method: HttpMethod): KoraHttpRequestHandler? = this.handlers[method]

    fun handleFull(handlerContext: ChannelHandlerContext, koraContext: KoraHttpContext) {
        // Launch on coroutine scope.
        this.executionScope.launch {
            val handler: KoraHttpRequestHandler? = handlers[koraContext.method()]
            if (handler == null) {
                // Notice user doesn't register this method handler (like POST, GET or ETC.) and let Kora framework handle this error.
                throw NotSupportedHttpMethodException("${koraContext.method().name()} handler not registered")
            } else {
                // Handle program logics.
                abortable(handlerContext, koraContext, handler) {
                    try {
                        response(
                            handlerContext,
                            koraContext,
                            handler.handle(koraContext)
                        )
                    } catch (e: Throwable) {
                        // Let user handle error if user registered error handler.
                        // Make abort reason.
                        val abortReason = AbortReason(
                            e, e.message ?: "Unhandled exception"
                        )
                        // When error, default status is 500 INTERNAL_SERVER_ERROR.
                        var httpStatus = HttpResponseStatus.INTERNAL_SERVER_ERROR

                        if (e is HttpPathNotRegisteredException) {
                            // When error is page path not registered, it should be 404 NOT_FOUND.
                            httpStatus = HttpResponseStatus.NOT_FOUND
                        }

                        // Handle abort control logic.
                        handler.handleAbort(
                            koraContext.abortWith(httpStatus),
                            abortReason
                        ) {
                            if (it is Unit) {
                                // Response formatted JSON error response when user deoesn't make a result.
                                response(handlerContext, koraContext, e)
                            } else {
                                // Response user result.
                                response(handlerContext, koraContext, it)
                            }
                        }
                    }
                }
            }
        }
    }

    fun handleExceptionCaught(handlerContext: ChannelHandlerContext, cause: Throwable) {
        // Response an error message.
        handlerContext.writeAndFlush(
            KoraHttpErrors.INTERNAL_SERVER_ERROR(
                HttpVersion.HTTP_1_0,
                cause,
                cause.message ?: "Unhandled internal server error"
            )
        ).addListener(ChannelFutureListener.CLOSE)
    }

    override fun response(handlerContext: ChannelHandlerContext, koraContext: KoraHttpContext, response: Any) {
        when (response) {
            is JSONObject -> {
                koraContext.withContentType(HttpContentTypes.JSON)
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

            is HTMLElement -> {
                response(handlerContext, koraContext) {
                    // Setting content type to HTML to render HTML page.
                    koraContext.withContentType(HttpContentTypes.HTML)
                    response.toString()
                }
            }

            is Throwable -> {
                responseFull(handlerContext, koraContext) {
                    KoraHttpErrors.adapter(
                        koraContext.protocolVersion(),
                        response
                    )
                }
            }

            else -> {
                responseJSON(handlerContext, koraContext) {
                    koraContext.withContentType(HttpContentTypes.JSON)
                    JSONEncoder.encode(response)
                }
            }
        }
    }

    private fun response(
        handlerContext: ChannelHandlerContext,
        koraContext: KoraHttpContext,
        response: KoraHttpContext.() -> String
    ) {
        val msg: String = response(koraContext)

        handlerContext.writeAndFlush(
            KoraHttpResponses.createDefaultResponse(
                koraContext.protocolVersion(), koraContext.status(), msg
            ).setContentType(koraContext.contentType()).setLength()
        ).also {
            if (koraContext.isPromiseClose()) {
                it.addListener(ChannelFutureListener.CLOSE)
            }
        }
    }

    private fun responseFull(
        handlerContext: ChannelHandlerContext,
        koraContext: KoraHttpContext,
        response: KoraHttpContext.() -> FullHttpResponse
    ) {
        val msg: FullHttpResponse = response(koraContext)

        handlerContext.writeAndFlush(
            msg.setContentType(koraContext.contentType()).setLength()
        ).also {
            if (koraContext.isPromiseClose()) {
                it.addListener(ChannelFutureListener.CLOSE)
            }
        }
    }

    private fun responseJSON(
        handlerContext: ChannelHandlerContext,
        koraContext: KoraHttpContext,
        responser: KoraHttpContext.() -> JSONObject
    ) {
        val sendingContext = koraContext.createInherited()

        val msg: JSONObject = instructHttpMetadata(responser(sendingContext), sendingContext)

        sendingContext.withContentType(HttpContentTypes.JSON)

        response(handlerContext, sendingContext) {
            JSONEncoder.renderJSON(msg)
        }
    }
}