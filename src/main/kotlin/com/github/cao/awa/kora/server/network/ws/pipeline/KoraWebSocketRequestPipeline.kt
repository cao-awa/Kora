package com.github.cao.awa.kora.server.network.ws.pipeline

import com.github.cao.awa.cason.codec.encoder.JSONEncoder
import com.github.cao.awa.cason.obj.JSONObject
import com.github.cao.awa.kora.server.network.http.KoraHttpServer
import com.github.cao.awa.kora.server.network.http.content.type.HttpContentTypes
import com.github.cao.awa.kora.server.network.http.context.KoraHttpContext
import com.github.cao.awa.kora.server.network.http.context.abort.KoraAbortHttpContext
import com.github.cao.awa.kora.server.network.http.control.abort.reason.AbortReason
import com.github.cao.awa.kora.server.network.http.error.KoraHttpError
import com.github.cao.awa.kora.server.network.http.exception.method.NotSupportedHttpMethodException
import com.github.cao.awa.kora.server.network.http.handler.KoraHttpRequestHandler
import com.github.cao.awa.kora.server.network.http.handler.get.KoraHttpGetHandler
import com.github.cao.awa.kora.server.network.http.handler.post.KoraHttpPostHandler
import com.github.cao.awa.kora.server.network.http.holder.KoraFullHttpRequestHolder
import com.github.cao.awa.kora.server.network.http.metadata.HttpResponseMetadata
import com.github.cao.awa.kora.server.network.http.response.KoraHttpResponses
import com.github.cao.awa.kora.server.network.http.response.KoraHttpResponses.setContentType
import com.github.cao.awa.kora.server.network.http.response.KoraHttpResponses.setLength
import com.github.cao.awa.kora.server.network.http.response.content.NoContentResponse
import com.github.cao.awa.kora.server.network.pipeline.KoraRequestPipeline
import com.github.cao.awa.kora.server.network.ws.context.KoraWebSocketContext
import com.github.cao.awa.kora.server.network.ws.context.abort.KoraAbortWebSocketContext
import com.github.cao.awa.kora.server.network.ws.handler.KoraWebSocketRequestHandler
import com.github.cao.awa.kora.server.network.ws.holder.KoraTextWebsocketFrameHolder
import com.github.cao.awa.kora.server.network.ws.response.KoraWebSocketResponses
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.HttpMethod
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpVersion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

class KoraWebSocketRequestPipeline :
    KoraRequestPipeline<KoraTextWebsocketFrameHolder, KoraWebSocketContext, KoraAbortWebSocketContext, KoraWebSocketRequestHandler>() {
    private val handler: KoraWebSocketRequestHandler = KoraWebSocketRequestHandler()
    private val executionScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun route(path: String, handler: KoraWebSocketContext.() -> Any) {
        this.handler.route(path, handler)
    }

    fun routeExceptionHandler(
        path: String,
        type: KClass<out Throwable>,
        handler: KoraAbortWebSocketContext.(AbortReason<out Throwable>) -> Any
    ) {
        this.handler.routeExceptionHandler(path, type, handler)
    }

    fun handle(handlerContext: ChannelHandlerContext, koraContext: KoraWebSocketContext) {
        // Launch on coroutine scope.
        this.executionScope.launch {
            val handler: KoraWebSocketRequestHandler = this@KoraWebSocketRequestPipeline.handler
            abortable(handlerContext, koraContext, handler) {
                if (handler.hasRoute(koraContext.path())) {
                    response(
                        handlerContext = handlerContext,
                        koraContext = koraContext,
                        response = handler.handle(koraContext)
                    )
                } else {
                    error("Unhandlable on path '${koraContext.path()}'")
                }
            }
        }
    }

    fun handleExceptionCaught(handlerContext: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        // Response an error message.
        handlerContext.writeAndFlush(
            KoraHttpError.INTERNAL_SERVER_ERROR(HttpVersion.HTTP_1_0, cause)
        ).addListener(ChannelFutureListener.CLOSE)
    }

    override fun response(handlerContext: ChannelHandlerContext, koraContext: KoraWebSocketContext, response: Any) {
        when (response) {
            is JSONObject -> {
                responseJSON(handlerContext, koraContext) {
                    response
                }
            }

            is NoContentResponse -> {
                response(handlerContext, koraContext) {
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
        koraContext: KoraWebSocketContext,
        response: KoraWebSocketContext.() -> String
    ) {
        val msg: String = response(koraContext)

        handlerContext.writeAndFlush(
            KoraWebSocketResponses.createDefaultResponse(
                msg
            )
        ).also {
            if (koraContext.isPromiseClose()) {
                it.addListener(ChannelFutureListener.CLOSE)
            }
        }
    }

    private fun responseJSON(
        handlerContext: ChannelHandlerContext,
        koraContext: KoraWebSocketContext,
        responser: KoraWebSocketContext.() -> JSONObject
    ) {
        val sendingContext = koraContext.createInherited()

        val msg: JSONObject = responser(sendingContext)

        response(handlerContext, sendingContext) {
            JSONEncoder.renderJSON(msg)
        }
    }
}