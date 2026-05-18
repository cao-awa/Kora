package com.github.cao.awa.kora.server.network.http.context

import com.github.cao.awa.cason.serialize.parser.StrictJSONParser
import com.github.cao.awa.kora.server.network.context.KoraContext
import com.github.cao.awa.kora.server.network.http.argument.HttpRequestArguments
import com.github.cao.awa.kora.server.network.http.content.type.HttpContentType
import com.github.cao.awa.kora.server.network.http.content.type.HttpContentTypes
import com.github.cao.awa.kora.server.network.http.context.abort.KoraAbortHttpContext
import com.github.cao.awa.kora.server.network.exception.abort.UnexpectedBehaviorException
import com.github.cao.awa.kora.server.network.http.asset.producer.KoraAssetProducer
import com.github.cao.awa.kora.server.network.http.form.encoded.UrlEncodedForm
import com.github.cao.awa.kora.server.network.http.holder.KoraFullHttpRequestHolder
import com.github.cao.awa.kora.server.network.http.param.HttpRequestParams
import com.github.cao.awa.kora.server.network.http.url.KoraPlaceholderURL
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpHeaderValues
import io.netty.handler.codec.http.HttpMethod
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpVersion
import org.jetbrains.annotations.Contract
import java.nio.charset.StandardCharsets

@Suppress("unused")
open class KoraHttpContext : KoraContext<KoraFullHttpRequestHolder, KoraHttpContext, KoraAbortHttpContext> {
    companion object {
        private val APPLICATION_JSON: String =
            HttpHeaderValues.APPLICATION_JSON.toString()
        private val APPLICATION_X_WWW_FORM_URLENCODED: String =
            HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString()

        private fun produceParams(msg: KoraFullHttpRequestHolder): HttpRequestParams {
            return when (msg.headers()[HttpHeaderNames.CONTENT_TYPE]) {
                APPLICATION_JSON -> {
                    HttpRequestParams.build(
                        StrictJSONParser.parseObject(
                            msg.content().toString(StandardCharsets.UTF_8)
                        )
                    )
                }

                APPLICATION_X_WWW_FORM_URLENCODED -> {
                    HttpRequestParams.build(
                        UrlEncodedForm.build(
                            msg.path().substringAfter("?")
                        )
                    )
                }

                else -> HttpRequestParams.EMPTY
            }
        }

        private fun produceArguments(msg: KoraFullHttpRequestHolder): HttpRequestArguments {
            return if (msg.path().contains("?")) {
                HttpRequestArguments.build(
                    UrlEncodedForm.build(
                        msg.path().substringAfter("?")
                    )
                )
            } else {
                HttpRequestArguments.EMPTY
            }
        }
    }

    private var arguments: HttpRequestArguments = HttpRequestArguments.EMPTY
    private var params: HttpRequestParams = HttpRequestParams.EMPTY
    private var promiseClose: Boolean = false
    private var status: HttpResponseStatus = HttpResponseStatus.OK
    private var contentType: HttpContentType = HttpContentTypes.PLAIN
    private var protocolVersion: HttpVersion = HttpVersion.HTTP_1_1
    private var method: HttpMethod = HttpMethod.GET
    private var path: String? = null
    private var placeholders: MutableMap<String, Int> = mutableMapOf()
    private var placeholderURL: String = ""
    private var redirectAsset: String = ""

    constructor(msg: KoraFullHttpRequestHolder) : super(msg) {
        this.arguments = produceArguments(msg)
        this.params = produceParams(msg)
        this.path = super.path().let {
            var result = it
            if (result.contains("?")) {
                result = result.substringBefore("?")
            }
            if (result.endsWith("/")) {
                result.substring(0, it.length - 1)
            } else {
                result
            }
        }
        this.method = msg.method()
    }

    constructor(context: KoraHttpContext) : super(context) {
        this.arguments = context.arguments
        this.params = context.params
        this.promiseClose = context.promiseClose
        this.status = context.status
        this.contentType = context.contentType
        this.protocolVersion = context.protocolVersion
        this.method = context.method
        this.path = context.path
        this.placeholders = context.placeholders
        this.placeholderURL = context.placeholderURL
        this.redirectAsset = context.redirectAsset
    }

    fun withAsset(redirectAsset: String): KoraAssetProducer {
        this.redirectAsset = redirectAsset
        return KoraAssetProducer(this)
    }

    fun withPlaceholder(url: KoraPlaceholderURL): KoraHttpContext {
        this.placeholders = url.placeholders()
        this.placeholderURL = url.toString()
        return this
    }

    open fun withStatus(status: HttpResponseStatus): KoraHttpContext {
        this.status = status
        return this
    }

    open fun withContentType(contentType: HttpContentType): KoraHttpContext {
        this.contentType = contentType
        return this
    }

    open fun withProtocolVersion(protocolVersion: HttpVersion): KoraHttpContext {
        this.protocolVersion = protocolVersion
        return this
    }

    fun redirectAsset(): String {
        return this.redirectAsset
    }

    fun placeholders(): Map<String, Int> {
        return this.placeholders
    }

    fun placeholderURL(): String {
        return this.placeholderURL
    }

    fun params(): HttpRequestParams {
        return this.params
    }

    fun arguments(): HttpRequestArguments {
        return this.arguments
    }

    open fun promiseClose() {
        this.promiseClose = true
    }

    fun isPromiseClose(): Boolean {
        return this.promiseClose
    }

    @Contract(pure = true)
    override fun path(): String {
        return this.path!!
    }

    fun createAbort(
        exception: Exception,
        errorCode: HttpResponseStatus,
        context: KoraHttpContext,
        postHandler: (KoraAbortHttpContext) -> Unit = { }
    ): KoraAbortHttpContext {
        when (errorCode) {
            HttpResponseStatus.OK -> error("Error response cannot use status '200 OK'")
        }
        withStatus(errorCode)
        withContentType(HttpContentTypes.JSON)
        postHandler(context.createAbort())
        return KoraAbortHttpContext(this).also {
            postHandler(it)
        }
    }

    fun createAbort(
        httpStatus: HttpResponseStatus,
        context: KoraHttpContext,
        postHandler: (KoraAbortHttpContext) -> Unit = { }
    ): KoraAbortHttpContext {
        return createAbort(
            UnexpectedBehaviorException(httpStatus.reasonPhrase()),
            httpStatus,
            context,
            postHandler
        )
    }

    fun abortWith(
        exception: Exception,
        errorCode: HttpResponseStatus,
        context: KoraHttpContext,
        postHandler: (KoraAbortHttpContext) -> Unit = { }
    ) {
        when (errorCode) {
            HttpResponseStatus.OK -> error("Error response cannot use status '200 OK'")
        }
        withStatus(errorCode)
        withContentType(HttpContentTypes.JSON)
        postHandler(context.createAbort())
        throw exception
    }

    fun abortWith(
        httpStatus: HttpResponseStatus,
        context: KoraHttpContext,
        postHandler: (KoraAbortHttpContext) -> Unit = { }
    ) {
        abortWith(
            UnexpectedBehaviorException(httpStatus.reasonPhrase()),
            httpStatus,
            context,
            postHandler
        )
    }

    fun status(): HttpResponseStatus {
        return this.status
    }

    fun contentType(): HttpContentType {
        return this.contentType
    }

    fun contentLength(): Int {
        return content().size
    }

    fun method(): HttpMethod {
        return this.method
    }

    fun protocolVersion(): HttpVersion {
        return this.protocolVersion
    }

    override fun createInherited(): KoraHttpContext {
        return KoraHttpContext(this)
    }

    override fun createAbort(): KoraAbortHttpContext {
        return KoraAbortHttpContext(this)
    }
}