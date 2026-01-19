package com.github.cao.awa.kora.server.network.ws.context

import com.github.cao.awa.cason.obj.JSONObject
import com.github.cao.awa.cason.serialize.parser.StrictJSONParser
import com.github.cao.awa.kora.server.network.context.KoraContext
import com.github.cao.awa.kora.server.network.http.argument.HttpRequestArguments
import com.github.cao.awa.kora.server.network.http.content.type.HttpContentType
import com.github.cao.awa.kora.server.network.http.content.type.HttpContentTypes
import com.github.cao.awa.kora.server.network.http.exception.abort.EndingEarlyException
import com.github.cao.awa.kora.server.network.http.form.encoded.UrlEncodedForm
import com.github.cao.awa.kora.server.network.http.holder.KoraFullHttpRequestHolder
import com.github.cao.awa.kora.server.network.http.param.HttpRequestParams
import com.github.cao.awa.kora.server.network.ws.context.abort.KoraAbortWebSocketContext
import com.github.cao.awa.kora.server.network.ws.holder.KoraTextWebsocketFrameHolder
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpHeaderValues
import io.netty.handler.codec.http.HttpMethod
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpVersion
import java.nio.charset.StandardCharsets

@Suppress("unused")
open class KoraWebSocketContext(val msg: KoraTextWebsocketFrameHolder): KoraContext<KoraTextWebsocketFrameHolder, KoraWebSocketContext, KoraAbortWebSocketContext>(msg) {
    companion object {

    }
    private var promiseClose: Boolean = false
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

    open fun promiseClose() {
        this.promiseClose = true
    }

    fun isPromiseClose(): Boolean {
        return this.promiseClose
    }

    fun abortWith(exception: Exception, postHandler: () -> Unit = { }) {
        postHandler()
        throw exception
    }

    fun abortWith(postHandler: () -> Unit = { }) {
        abortWith(
            EndingEarlyException(),
            postHandler
        )
    }

    fun abortIf(condition: Boolean, postHandler: () -> Unit = { }) {
        if (condition) {
            abortWith(postHandler)
        }
    }

    override fun createInherited(): KoraWebSocketContext {
        return KoraWebSocketContext(this.msg).also {
            it.promiseClose = this.promiseClose
        }
    }

    override fun createAbort(): KoraAbortWebSocketContext {
        return KoraAbortWebSocketContext(this)
    }
}