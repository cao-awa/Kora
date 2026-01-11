package com.github.cao.awa.kora.server.network.http.handler

import com.github.cao.awa.cason.codec.encoder.JSONEncoder
import com.github.cao.awa.cason.obj.JSONObject
import com.github.cao.awa.kora.server.network.http.KoraHttpServer
import com.github.cao.awa.kora.server.network.http.content.type.HttpContentTypes
import com.github.cao.awa.kora.server.network.http.metadata.HttpResponseMetadata
import com.github.cao.awa.kora.server.network.http.response.KoraHttpResponses
import com.github.cao.awa.kora.server.network.http.response.KoraHttpResponses.setContentType
import com.github.cao.awa.kora.server.network.http.response.KoraHttpResponses.setLength
import com.github.cao.awa.kora.server.network.response.KoraContext
import com.github.cao.awa.kora.server.network.response.content.NoContentResponse
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.HttpMethod
import io.netty.handler.codec.http.HttpResponseStatus

class KoraHttpRequestHandler {
    private val postRoutes: MutableMap<String, KoraContext.() -> Any> = mutableMapOf()
    private val getRoutes: MutableMap<String, KoraContext.() -> Any> = mutableMapOf()

    fun routePost(path: String, handler: KoraContext.() -> Any): KoraHttpRequestHandler {
        this.postRoutes[path] = handler
        return this
    }

    fun routeGet(path: String, handler: KoraContext.() -> Any): KoraHttpRequestHandler {
        this.getRoutes[path] = handler
        return this
    }

    fun handleFull(handlerContext: ChannelHandlerContext, koraContext: KoraContext) {
        when (koraContext.method()) {
            HttpMethod.POST -> handlePost(handlerContext, koraContext)
            HttpMethod.GET -> handleGet(handlerContext, koraContext)
            else -> {}
        }
    }

    private fun handlePost(handlerContext: ChannelHandlerContext, context: KoraContext) {
        this.postRoutes[context.path()]?.let {
            response(handlerContext, context, it(context))
        }
    }

    private fun handleGet(handlerContext: ChannelHandlerContext, context: KoraContext) {
        this.getRoutes[context.path()]?.let {
            response(handlerContext, context, it(context))
        }
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
                    koraContext.status = HttpResponseStatus.NO_CONTENT

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
                koraContext.protocolVersion,
                koraContext.status,
                msg
            ).setContentType(koraContext.contentType)
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
        val msg: JSONObject = responser(koraContext).instruct {
            if (KoraHttpServer.instructHttpMetadata) {
                nested("http_meta") {
                    HttpResponseMetadata(
                        if (KoraHttpServer.instructHttpStatusCode) {
                            koraContext.status.code()
                        } else null,
                        if (KoraHttpServer.instructHttpVersionCode) {
                            koraContext.protocolVersion.text()
                        } else null
                    )
                }
            }
        }

        koraContext.contentType = HttpContentTypes.JSON

        response(handlerContext, koraContext) {
            JSONEncoder.encodeJSON(msg)
        }
    }
}