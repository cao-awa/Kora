package com.github.cao.awa.kora.server.network.http.context

import com.github.cao.awa.cason.serialize.parser.StrictJSONParser
import com.github.cao.awa.kora.server.network.context.KoraContext
import com.github.cao.awa.kora.server.network.http.argument.HttpRequestArguments
import com.github.cao.awa.kora.server.network.http.content.type.HttpContentType
import com.github.cao.awa.kora.server.network.http.content.type.HttpContentTypes
import com.github.cao.awa.kora.server.network.http.context.abort.KoraAbortHttpContext
import com.github.cao.awa.kora.server.network.exception.abort.EndingEarlyException
import com.github.cao.awa.kora.server.network.http.form.encoded.UrlEncodedForm
import com.github.cao.awa.kora.server.network.http.holder.KoraFullHttpRequestHolder
import com.github.cao.awa.kora.server.network.http.param.HttpRequestParams
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpHeaderValues
import io.netty.handler.codec.http.HttpMethod
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpVersion
import java.nio.charset.StandardCharsets

@Suppress("unused")
open class KoraHttpContext(val msg: KoraFullHttpRequestHolder): KoraContext<KoraFullHttpRequestHolder, KoraHttpContext, KoraAbortHttpContext>(msg) {
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

    private val arguments: HttpRequestArguments = produceArguments(this.msg)
    private val params: HttpRequestParams = produceParams(this.msg)
    private var promiseClose: Boolean = false
    private var status: HttpResponseStatus = HttpResponseStatus.OK
    private var contentType: HttpContentType = HttpContentTypes.PLAIN
    private var protocolVersion: HttpVersion = HttpVersion.HTTP_1_1
    private var path: String = path().let {
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

    open fun withStatus(status: HttpResponseStatus) {
        this.status = status
    }

    open fun withContentType(contentType: HttpContentType) {
        this.contentType = contentType
    }

    open fun withProtocolVersion(protocolVersion: HttpVersion) {
        this.protocolVersion = protocolVersion
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

    fun abortWith(exception: Exception, errorCode: HttpResponseStatus, postHandler: () -> Unit = { }) {
        when (errorCode) {
            HttpResponseStatus.OK -> error("Error response cannot use status '200 OK'")
        }
        withStatus(errorCode)
        withContentType(HttpContentTypes.JSON)
        postHandler()
        throw exception
    }

    fun abortWith(errorCode: HttpResponseStatus, postHandler: () -> Unit = { }) {
        abortWith(
            EndingEarlyException(),
            errorCode,
            postHandler
        )
    }

    fun abortIf(condition: Boolean, errorCode: HttpResponseStatus, postHandler: () -> Unit = { }) {
        if (condition) {
            abortWith(errorCode, postHandler)
        }
    }

    fun status(): HttpResponseStatus {
        return this.status
    }

    fun contentType(): HttpContentType {
        return this.contentType
    }

    fun method(): HttpMethod {
        return this.msg.method()
    }

    fun protocolVersion(): HttpVersion {
        return this.msg.protocolVersion()
    }

    override fun createInherited(): KoraHttpContext {
        return KoraHttpContext(this.msg).also {
            it.status = this.status
            it.contentType = this.contentType
            it.protocolVersion = this.protocolVersion
            it.promiseClose = this.promiseClose
        }
    }

    override fun createAbort(): KoraAbortHttpContext {
        return KoraAbortHttpContext(this)
    }
}