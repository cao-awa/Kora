package com.github.cao.awa.kora.server.network.http.context

import com.github.cao.awa.cason.serialize.parser.JSONParser
import com.github.cao.awa.kora.server.network.context.KoraContext
import com.github.cao.awa.kora.server.network.http.argument.HttpRequestArguments
import com.github.cao.awa.kora.server.network.http.content.type.HttpContentType
import com.github.cao.awa.kora.server.network.http.content.type.HttpContentTypes
import com.github.cao.awa.kora.server.network.http.context.abort.KoraAbortHttpContext
import com.github.cao.awa.kora.server.network.exception.abort.UnexpectedBehaviorException
import com.github.cao.awa.kora.server.network.http.argument.type.TypedHttpArgument
import com.github.cao.awa.kora.server.network.http.asset.producer.KoraAssetProducer
import com.github.cao.awa.kora.server.network.http.body.KoraHttpBody
import com.github.cao.awa.kora.server.network.http.body.empty.KoraHttpEmptyBody
import com.github.cao.awa.kora.server.network.http.body.exception.UnhandleableRequestBodyException
import com.github.cao.awa.kora.server.network.http.body.form.urlencoded.KoraHttpUrlencodedBody
import com.github.cao.awa.kora.server.network.http.body.json.KoraHttpJsonBody
import com.github.cao.awa.kora.server.network.http.body.text.KoraHttpTextBody
import com.github.cao.awa.kora.server.network.http.form.encoded.UrlEncodedForm
import com.github.cao.awa.kora.server.network.http.header.value.KoraHttpHeaderValues
import com.github.cao.awa.kora.server.network.http.holder.KoraFullHttpRequestHolder
import com.github.cao.awa.kora.server.network.http.placeholder.url.type.TypedHttpUrlPlaceholder
import com.github.cao.awa.kora.server.network.http.url.KoraPlaceholderURL
import io.netty.handler.codec.http.DefaultHttpHeaders
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpHeaders
import io.netty.handler.codec.http.HttpMethod
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpVersion
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.jetbrains.annotations.Contract
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.Random

@Suppress("unused")
open class KoraHttpContext : KoraContext<KoraFullHttpRequestHolder, KoraHttpContext, KoraAbortHttpContext> {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger("KoraHttpContext")
        private val RANDOM: Random = Random()

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

    private var requestId: Long = RANDOM.nextLong()
    private var arguments: HttpRequestArguments = HttpRequestArguments.EMPTY
    private var headers: HttpHeaders = DefaultHttpHeaders()
    private var responseHeaders: HttpHeaders = DefaultHttpHeaders()
    private var body: KoraHttpBody? = null
    private val bodyBuilder: () -> Unit
    private var promiseClose: Boolean = false
    private var status: HttpResponseStatus = HttpResponseStatus.OK
    private var contentType: HttpContentType = HttpContentTypes.PLAIN
    private var protocolVersion: HttpVersion = HttpVersion.HTTP_1_1
    private var method: HttpMethod = HttpMethod.GET
    private var path: String? = null
    private var placeholders: MutableMap<String, Int> = mutableMapOf()
    private var placeholderURL: String = ""
    private var redirectAsset: String = ""
    private var redirectUrl: String = ""
    private var dataCache: MutableMap<String, Any> = mutableMapOf()

    constructor(msg: KoraFullHttpRequestHolder) : super(msg) {
        this.arguments = produceArguments(msg)
        this.headers = msg.headers()
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
        this.bodyBuilder = {
            if (this.body == null) {
                if (msg.content().readableBytes() == 0) {
                    this.body = KoraHttpEmptyBody
                } else {
                    var contentType = this.headers[HttpHeaderNames.CONTENT_TYPE]
                    println(contentType)
                    var charset = StandardCharsets.UTF_8
                    if (contentType.contains(";")) {
                        for (data in contentType.split(";")) {
                            if (data.contains("charset=")) {
                                charset = Charset.forName(data.substringAfter("charset=").trim())
                                break
                            }
                        }
                        contentType = contentType.substringBefore(";")
                    }
                    try {
                        when (contentType) {
                            KoraHttpHeaderValues.APPLICATION_JSON -> {
                                this.body = KoraHttpJsonBody(JSONParser.parseObject(msg.content().toString(charset)))
                            }

                            KoraHttpHeaderValues.TEXT_PLAIN -> {
                                this.body = KoraHttpTextBody(msg.content().toString(charset))
                            }

                            KoraHttpHeaderValues.X_WWW_FORM_URLENCODED -> {
                                this.body = KoraHttpUrlencodedBody.build(
                                    UrlEncodedForm.build(msg.content().toString(charset))
                                )
                            }

                            else -> {
                                throw UnhandleableRequestBodyException("Unsupported content type: $contentType")
                            }
                        }
                    } catch (e: Exception) {
                        LOGGER.warn(
                            "Unable to handle request body '{}', data: {}",
                            contentType,
                            msg.content().toString(charset),
                            e
                        )
                        LOGGER.warn("Please check the body data is correct format or report this content type to issue")
                    }
                }
            }
        }
        this.method = msg.method()
    }

    constructor(context: KoraHttpContext) : super(context) {
        this.arguments = context.arguments
        this.headers = DefaultHttpHeaders()
        this.responseHeaders = DefaultHttpHeaders()
        this.bodyBuilder = {
            this.body = KoraHttpEmptyBody
        }
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

    fun cache(key: String, data: Any): KoraHttpContext {
        this.dataCache[key] = data
        return this
    }

    fun fetchCache(key: String): Any? {
        return this.dataCache[key]
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
        when (status) {
            HttpResponseStatus.MOVED_PERMANENTLY,
            HttpResponseStatus.PERMANENT_REDIRECT,
            HttpResponseStatus.TEMPORARY_REDIRECT -> {
                responseHeaders()[HttpHeaderNames.LOCATION] = this.redirectUrl
            }
        }
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

    fun redirect(redirectUrl: String, permanently: Boolean = false): KoraHttpContext {
        return if (permanently) {
            permanentlyRedirect(redirectUrl)
        } else {
            temporaryRedirect(redirectUrl)
        }
    }

    fun temporaryRedirect(redirectUrl: String): KoraHttpContext {
        this.status = HttpResponseStatus.TEMPORARY_REDIRECT
        this.redirectUrl = redirectUrl
        return this
    }

    fun permanentlyRedirect(redirectUrl: String): KoraHttpContext {
        this.status = HttpResponseStatus.PERMANENT_REDIRECT
        this.redirectUrl = redirectUrl
        return this
    }

    fun movedPermanently(redirectUrl: String): KoraHttpContext {
        this.status = HttpResponseStatus.MOVED_PERMANENTLY
        this.redirectUrl = redirectUrl
        return this
    }

    fun redirectAsset(): String {
        return this.redirectAsset
    }

    fun redirectUrl(): String {
        return this.redirectUrl
    }

    fun placeholders(): Map<String, Int> {
        return this.placeholders
    }

    fun placeholderURL(): String {
        return this.placeholderURL
    }

    fun arguments(): HttpRequestArguments {
        return this.arguments
    }

    fun body(): KoraHttpBody {
        if (this.body == null) {
            this.bodyBuilder()
        }
        return this.body!!
    }

    fun getHeader(name: String): String? {
        return this.headers[name]
    }

    fun headers(): HttpHeaders {
        return this.headers.copy()
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

    fun responseHeaders(): HttpHeaders {
        return this.responseHeaders
    }

    override fun createInherited(): KoraHttpContext {
        return KoraHttpContext(this)
    }

    override fun createAbort(): KoraAbortHttpContext {
        return KoraAbortHttpContext(this)
    }

    // Helper methods.

    // Typed arg.
    inline fun <reified R : Any, reified T1 : Any> build(
        arg1: TypedHttpArgument<T1>,
        builder: (T1) -> R
    ): R {
        return builder(arg1[this])
    }

    inline fun <reified R : Any, reified T1 : Any, reified T2 : Any> build(
        arg1: TypedHttpArgument<T1>,
        arg2: TypedHttpArgument<T2>,
        builder: (T1, T2) -> R
    ): R {
        return builder(arg1[this], arg2[this])
    }

    inline fun <reified R : Any, reified T1 : Any, reified T2 : Any, reified T3 : Any> build(
        arg1: TypedHttpArgument<T1>,
        arg2: TypedHttpArgument<T2>,
        arg3: TypedHttpArgument<T3>,
        builder: (T1, T2, T3) -> R
    ): R {
        return builder(arg1[this], arg2[this], arg3[this])
    }

    inline fun <reified R : Any, reified T1 : Any, reified T2 : Any, reified T3 : Any, reified T4 : Any> build(
        arg1: TypedHttpArgument<T1>,
        arg2: TypedHttpArgument<T2>,
        arg3: TypedHttpArgument<T3>,
        arg4: TypedHttpArgument<T4>,
        builder: (T1, T2, T3, T4) -> R
    ): R {
        return builder(arg1[this], arg2[this], arg3[this], arg4[this])
    }

    inline fun <
            reified R : Any,
            reified T1 : Any,
            reified T2 : Any,
            reified T3 : Any,
            reified T4 : Any,
            reified T5 : Any
            > build(
        arg1: TypedHttpArgument<T1>,
        arg2: TypedHttpArgument<T2>,
        arg3: TypedHttpArgument<T3>,
        arg4: TypedHttpArgument<T4>,
        arg5: TypedHttpArgument<T5>,
        builder: (T1, T2, T3, T4, T5) -> R
    ): R {
        return builder(arg1[this], arg2[this], arg3[this], arg4[this], arg5[this])
    }

    inline fun <
            reified R : Any,
            reified T1 : Any,
            reified T2 : Any,
            reified T3 : Any,
            reified T4 : Any,
            reified T5 : Any,
            reified T6 : Any
            > build(
        arg1: TypedHttpArgument<T1>,
        arg2: TypedHttpArgument<T2>,
        arg3: TypedHttpArgument<T3>,
        arg4: TypedHttpArgument<T4>,
        arg5: TypedHttpArgument<T5>,
        arg6: TypedHttpArgument<T6>,
        builder: (T1, T2, T3, T4, T5, T6) -> R
    ): R {
        return builder(arg1[this], arg2[this], arg3[this], arg4[this], arg5[this], arg6[this])
    }

    inline fun <
            reified R : Any,
            reified T1 : Any,
            reified T2 : Any,
            reified T3 : Any,
            reified T4 : Any,
            reified T5 : Any,
            reified T6 : Any,
            reified T7 : Any
            > build(
        arg1: TypedHttpArgument<T1>,
        arg2: TypedHttpArgument<T2>,
        arg3: TypedHttpArgument<T3>,
        arg4: TypedHttpArgument<T4>,
        arg5: TypedHttpArgument<T5>,
        arg6: TypedHttpArgument<T6>,
        arg7: TypedHttpArgument<T7>,
        builder: (T1, T2, T3, T4, T5, T6, T7) -> R
    ): R {
        return builder(arg1[this], arg2[this], arg3[this], arg4[this], arg5[this], arg6[this], arg7[this])
    }

    // Typed placeholder.
    inline fun <reified R : Any, reified T1 : Any> build(
        arg1: TypedHttpUrlPlaceholder<T1>,
        builder: (T1) -> R
    ): R {
        return builder(arg1[this])
    }

    inline fun <reified R : Any, reified T1 : Any, reified T2 : Any> build(
        arg1: TypedHttpUrlPlaceholder<T1>,
        arg2: TypedHttpUrlPlaceholder<T2>,
        builder: (T1, T2) -> R
    ): R {
        return builder(arg1[this], arg2[this])
    }

    inline fun <reified R : Any, reified T1 : Any, reified T2 : Any, reified T3 : Any> build(
        arg1: TypedHttpUrlPlaceholder<T1>,
        arg2: TypedHttpUrlPlaceholder<T2>,
        arg3: TypedHttpUrlPlaceholder<T3>,
        builder: (T1, T2, T3) -> R
    ): R {
        return builder(arg1[this], arg2[this], arg3[this])
    }

    inline fun <reified R : Any, reified T1 : Any, reified T2 : Any, reified T3 : Any, reified T4 : Any> build(
        arg1: TypedHttpUrlPlaceholder<T1>,
        arg2: TypedHttpUrlPlaceholder<T2>,
        arg3: TypedHttpUrlPlaceholder<T3>,
        arg4: TypedHttpUrlPlaceholder<T4>,
        builder: (T1, T2, T3, T4) -> R
    ): R {
        return builder(arg1[this], arg2[this], arg3[this], arg4[this])
    }

    inline fun <
            reified R : Any,
            reified T1 : Any,
            reified T2 : Any,
            reified T3 : Any,
            reified T4 : Any,
            reified T5 : Any
            > build(
        arg1: TypedHttpUrlPlaceholder<T1>,
        arg2: TypedHttpUrlPlaceholder<T2>,
        arg3: TypedHttpUrlPlaceholder<T3>,
        arg4: TypedHttpUrlPlaceholder<T4>,
        arg5: TypedHttpUrlPlaceholder<T5>,
        builder: (T1, T2, T3, T4, T5) -> R
    ): R {
        return builder(arg1[this], arg2[this], arg3[this], arg4[this], arg5[this])
    }

    inline fun <
            reified R : Any,
            reified T1 : Any,
            reified T2 : Any,
            reified T3 : Any,
            reified T4 : Any,
            reified T5 : Any,
            reified T6 : Any
            > build(
        arg1: TypedHttpUrlPlaceholder<T1>,
        arg2: TypedHttpUrlPlaceholder<T2>,
        arg3: TypedHttpUrlPlaceholder<T3>,
        arg4: TypedHttpUrlPlaceholder<T4>,
        arg5: TypedHttpUrlPlaceholder<T5>,
        arg6: TypedHttpUrlPlaceholder<T6>,
        builder: (T1, T2, T3, T4, T5, T6) -> R
    ): R {
        return builder(arg1[this], arg2[this], arg3[this], arg4[this], arg5[this], arg6[this])
    }

    inline fun <
            reified R : Any,
            reified T1 : Any,
            reified T2 : Any,
            reified T3 : Any,
            reified T4 : Any,
            reified T5 : Any,
            reified T6 : Any,
            reified T7 : Any
            > build(
        arg1: TypedHttpUrlPlaceholder<T1>,
        arg2: TypedHttpUrlPlaceholder<T2>,
        arg3: TypedHttpUrlPlaceholder<T3>,
        arg4: TypedHttpUrlPlaceholder<T4>,
        arg5: TypedHttpUrlPlaceholder<T5>,
        arg6: TypedHttpUrlPlaceholder<T6>,
        arg7: TypedHttpUrlPlaceholder<T7>,
        builder: (T1, T2, T3, T4, T5, T6, T7) -> R
    ): R {
        return builder(arg1[this], arg2[this], arg3[this], arg4[this], arg5[this], arg6[this], arg7[this])
    }

    // Direct build.
    // Typed arg.
    inline fun <reified R : Any, reified T1 : Any> build(
        arg1: T1,
        builder: (T1) -> R
    ): R {
        return builder(arg1)
    }

    inline fun <reified R : Any, reified T1 : Any, reified T2 : Any> build(
        arg1: T1,
        arg2: T2,
        builder: (T1, T2) -> R
    ): R {
        return builder(arg1, arg2)
    }

    inline fun <reified R : Any, reified T1 : Any, reified T2 : Any, reified T3 : Any> build(
        arg1: T1,
        arg2: T2,
        arg3: T3,
        builder: (T1, T2, T3) -> R
    ): R {
        return builder(arg1, arg2, arg3)
    }

    inline fun <reified R : Any, reified T1 : Any, reified T2 : Any, reified T3 : Any, reified T4 : Any> build(
        arg1: T1,
        arg2: T2,
        arg3: T3,
        arg4: T4,
        builder: (T1, T2, T3, T4) -> R
    ): R {
        return builder(arg1, arg2, arg3, arg4)
    }

    inline fun <
            reified R : Any,
            reified T1 : Any,
            reified T2 : Any,
            reified T3 : Any,
            reified T4 : Any,
            reified T5 : Any
            > build(
        arg1: T1,
        arg2: T2,
        arg3: T3,
        arg4: T4,
        arg5: T5,
        builder: (T1, T2, T3, T4, T5) -> R
    ): R {
        return builder(arg1, arg2, arg3, arg4, arg5)
    }

    inline fun <
            reified R : Any,
            reified T1 : Any,
            reified T2 : Any,
            reified T3 : Any,
            reified T4 : Any,
            reified T5 : Any,
            reified T6 : Any
            > build(
        arg1: T1,
        arg2: T2,
        arg3: T3,
        arg4: T4,
        arg5: T5,
        arg6: T6,
        builder: (T1, T2, T3, T4, T5, T6) -> R
    ): R {
        return builder(arg1, arg2, arg3, arg4, arg5, arg6)
    }

    inline fun <
            reified R : Any,
            reified T1 : Any,
            reified T2 : Any,
            reified T3 : Any,
            reified T4 : Any,
            reified T5 : Any,
            reified T6 : Any,
            reified T7 : Any
            > build(
        arg1: T1,
        arg2: T2,
        arg3: T3,
        arg4: T4,
        arg5: T5,
        arg6: T6,
        arg7: T7,
        builder: (T1, T2, T3, T4, T5, T6, T7) -> R
    ): R {
        return builder(arg1, arg2, arg3, arg4, arg5, arg6, arg7)
    }
}