package com.github.cao.awa.kora.server.network.http.pipeline

import com.github.cao.awa.cason.codec.encoder.JSONEncoder
import com.github.cao.awa.cason.obj.JSONObject
import com.github.cao.awa.kora.launch.config.KoraLaunchConfig
import com.github.cao.awa.kora.server.network.KoraNetworkConfig
import com.github.cao.awa.kora.server.network.http.KoraHttpServer
import com.github.cao.awa.kora.server.network.http.asset.KoraAsset
import com.github.cao.awa.kora.server.network.http.asset.config.KoraAssetManagerConfig
import com.github.cao.awa.kora.server.network.http.asset.producer.KoraAssetProducer
import com.github.cao.awa.kora.server.network.http.asset.manager.KoraHttpAssetsManager
import com.github.cao.awa.kora.server.network.http.content.type.HttpContentTypes
import com.github.cao.awa.kora.server.network.http.context.KoraHttpContext
import com.github.cao.awa.kora.server.network.http.context.abort.KoraAbortHttpContext
import com.github.cao.awa.kora.server.network.http.error.KoraHttpErrors
import com.github.cao.awa.kora.server.network.http.exception.KoraServerException
import com.github.cao.awa.kora.server.network.http.exception.method.NotSupportedHttpMethodException
import com.github.cao.awa.kora.server.network.http.handler.KoraHttpRequestHandler
import com.github.cao.awa.kora.server.network.http.handler.KoraHttpRequestServerAbortHandler
import com.github.cao.awa.kora.server.network.http.handler.get.KoraHttpGetHandler
import com.github.cao.awa.kora.server.network.http.handler.post.KoraHttpPostHandler
import com.github.cao.awa.kora.server.network.http.holder.KoraFullHttpRequestHolder
import com.github.cao.awa.kora.server.network.http.metadata.HttpResponseMetadata
import com.github.cao.awa.kora.server.network.http.exception.path.HttpPathNotRegisteredException
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

class
KoraHttpRequestPipeline(
    private val serverAbortHandlers: KoraHttpRequestServerAbortHandler,
    private val launchConfig: KoraLaunchConfig
) :
    KoraRequestPipeline<KoraFullHttpRequestHolder, KoraHttpContext, KoraAbortHttpContext, KoraHttpRequestHandler>() {
    companion object {
        fun instructHttpMetadata(json: JSONObject, context: KoraHttpContext): JSONObject {
            json.instruct {
                if (KoraHttpServer.instructRequestType) {
                    "request_type" set context.method().name()
                }
                if (KoraHttpServer.instructRequestPath) {
                    "request_path" set context.path()
                }
                instructHttpMetadata(
                    this,
                    context.status(),
                    context.protocolVersion(),
                    context.path()
                )
            }

            return json
        }

        fun instructHttpMetadata(
            json: JSONObject,
            status: HttpResponseStatus,
            protocolVersion: HttpVersion,
            requestPath: String
        ): JSONObject {
            json.instruct {
                if (KoraNetworkConfig.instructTimestamp) {
                    "timestamp" set System.currentTimeMillis()
                }
                if (KoraHttpServer.instructRequestType) {
                    "request_path" set requestPath
                }
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

    private val assetManagerConfig: KoraAssetManagerConfig = this.launchConfig.assetManagerConfig()
    private val handlers: Map<HttpMethod, KoraHttpRequestHandler> =
        HashMap<HttpMethod, KoraHttpRequestHandler>().apply {
            put(HttpMethod.GET, KoraHttpGetHandler())
            put(HttpMethod.POST, KoraHttpPostHandler())
        }
    private val assetsManager: KoraHttpAssetsManager = KoraHttpAssetsManager()
    private val executionScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun setAssetsPath(path: String) {
        this.assetsManager.setAssetsPath(path)
    }

    fun getAsset(context: KoraHttpContext): KoraAsset<*> {
        return this.assetsManager.getAsset(context)
    }

    fun getHandler(method: HttpMethod): KoraHttpRequestHandler? = this.handlers[method]

    fun handleFull(handlerContext: ChannelHandlerContext, koraContext: KoraHttpContext) {
        // Launch on coroutine scope.
        this.executionScope.launch {
            val handler: KoraHttpRequestHandler? = handlers[koraContext.method()]
            if (handler != null) {
                // Handle program logics.
                abortable(handlerContext, koraContext, handler) {
                    try {
                        // Try to create a result and response the result.
                        response(
                            handlerContext,
                            koraContext,
                            handler.handle(koraContext)
                        )
                    } catch (e: Throwable) {
                        // When error, default status is 500 INTERNAL_SERVER_ERROR.
                        var httpStatus = HttpResponseStatus.INTERNAL_SERVER_ERROR

                        // When path not registered, use asset manager to delegate the response.
                        if (e is HttpPathNotRegisteredException) {
                            if (assetManagerConfig.enable() && assetsManager.available()) {
                                val asset: KoraAsset<*>? = if (assetsManager.hasAsset(koraContext)) {
                                    assetsManager.getAsset(koraContext)
                                } else {
                                    assetsManager.getAsset(koraContext.path() + "/index.html")
                                }

                                // If asset not null. response the asset.
                                if (asset != null) {
                                    response(
                                        handlerContext,
                                        koraContext,
                                        asset
                                    )
                                }
                                return@abortable
                            } else {
                                // When error is page path not registered and asset manager are not available, it should be 404 NOT_FOUND.
                                httpStatus = HttpResponseStatus.NOT_FOUND
                            }
                        }

                        // Let user handle error if user registered error handler.
                        val abortContext = koraContext.createAbort(httpStatus, koraContext)

                        if (e is KoraServerException) {
                            // Handle server level exception (like 404 NOT_FOUND).
                            response(
                                handlerContext,
                                koraContext,
                                serverAbortHandlers.handleAbort(
                                    abortContext,
                                    e
                                )
                            )
                        } else {
                            // Handle abort control logic.
                            handler.handleAbort(
                                abortContext,
                                e
                            ) {
                                // Response formatted JSON error or user result.
                                responseExceptionOrData(handlerContext, koraContext, it, e)
                            }
                        }
                    }
                }
            } else {
                // Notice user doesn't register this method handler (like POST, GET or ETC.) and let Kora framework handle this error.
                throw NotSupportedHttpMethodException("${koraContext.method().name()} handler not registered")
            }

            // Release the msg let GC could be clears,
            koraContext.release()
        }
    }

    fun responseExceptionOrData(
        handlerContext: ChannelHandlerContext,
        koraContext: KoraHttpContext,
        response: Any,
        exception: Throwable
    ) {
        if (response is Unit) {
            // Response formatted JSON error response when user doesn't make a result.
            response(handlerContext, koraContext, exception)
        } else {
            // Response user result.
            response(handlerContext, koraContext, response)
        }
    }

    fun handleExceptionCaught(handlerContext: ChannelHandlerContext, cause: Throwable) {
        // Response an error message.
        handlerContext.writeAndFlush(
            KoraHttpErrors.INTERNAL_SERVER_ERROR(
                HttpVersion.HTTP_1_0,
                cause,
                cause.message ?: "Unhandled internal server error",
                "{UNKNOWN}",
                null
            )
        ).addListener(ChannelFutureListener.CLOSE)
    }

    override fun handleException(
        exception: Throwable,
        handlerContext: ChannelHandlerContext,
        koraContext: KoraHttpContext
    ) {
        // If not handleable, response an formatted error message by KoraHttpErrors.adapter formatter.
        if (koraContext.status() == HttpResponseStatus.OK) {
            koraContext.withStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR)
        }
        koraContext.withContentType(HttpContentTypes.JSON)
        handlerContext.writeAndFlush(
            KoraHttpErrors.adapter(
                HttpVersion.HTTP_1_0,
                exception,
                koraContext
            ).setContentType(HttpContentTypes.JSON).setLength()
        ).addListener(ChannelFutureListener.CLOSE)
    }

    override fun response(handlerContext: ChannelHandlerContext, koraContext: KoraHttpContext, response: Any) {
        when (response) {
            is JSONObject -> {
                responseJSON(handlerContext, koraContext) {
                    response
                }
            }

            is String -> {
                response(handlerContext, koraContext) {
                    koraContext.withContentType(HttpContentTypes.HTML)
                    response
                }
            }

            is NoContentResponse -> {
                responseNoContent(handlerContext, koraContext)
            }

            is HTMLElement -> {
                response(handlerContext, koraContext) {
                    // Setting content type to HTML to render HTML page.
                    koraContext.withContentType(HttpContentTypes.HTML)
                    response.toString()
                }
            }

            is KoraAsset<*>, is KoraAssetProducer -> {
                val data: KoraAsset<*> = if (response is KoraAssetProducer) {
                    response.getAsset(this@KoraHttpRequestPipeline)
                } else {
                    response as KoraAsset<*>
                }
                response(
                    handlerContext,
                    koraContext,
                    assetsManager.createResponse(koraContext, data)
                )
            }

            is Throwable -> {
                responseFull(handlerContext, koraContext) {
                    koraContext.withContentType(HttpContentTypes.JSON)
                    KoraHttpErrors.adapter(
                        koraContext.protocolVersion(),
                        koraContext.path(),
                        response
                    )
                }
            }

            is KoraHttpContext -> {
                response(
                    handlerContext,
                    koraContext,
                    assetsManager.createResponse(koraContext)
                )
            }

            is Unit -> {
                responseJSON(handlerContext, koraContext) {
                    JSONObject()
                }
            }

            is ByteArray -> {
                responseRaw(handlerContext, koraContext) {
                    response
                }
            }

            else -> {
                responseJSON(handlerContext, koraContext) {
                    JSONEncoder.encode(response)
                }
            }
        }
    }

    fun responseNoContent(handlerContext: ChannelHandlerContext, koraContext: KoraHttpContext) {
        response(handlerContext, koraContext) {
            // Force be no content status when response is no body response.
            koraContext.withStatus(HttpResponseStatus.NO_CONTENT)
            ""
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

    private fun responseRaw(
        handlerContext: ChannelHandlerContext,
        koraContext: KoraHttpContext,
        response: KoraHttpContext.() -> ByteArray
    ) {
        val msg = response(koraContext)

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

        val msg: JSONObject = instructHttpMetadata(JSONObject(), sendingContext)
        val data = responser(sendingContext)
        if (data.size() > 0) {
            msg.instruct {
                "data" set data
            }
        }

        sendingContext.withContentType(HttpContentTypes.JSON)

        response(handlerContext, sendingContext) {
            JSONEncoder.renderJSON(msg)
        }
    }
}