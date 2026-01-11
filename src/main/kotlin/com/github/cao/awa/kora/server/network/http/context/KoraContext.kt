package com.github.cao.awa.kora.server.network.http.context

import com.github.cao.awa.cason.obj.JSONObject
import com.github.cao.awa.cason.serialize.parser.StrictJSONParser
import com.github.cao.awa.kora.server.network.http.content.type.HttpContentType
import com.github.cao.awa.kora.server.network.http.content.type.HttpContentTypes
import com.github.cao.awa.kora.server.network.http.control.end.EndingEarlyException
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpMethod
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpVersion
import java.nio.charset.StandardCharsets

open class KoraContext(val msg: FullHttpRequest) {
    var promiseClose: Boolean = false
    private var status: HttpResponseStatus = HttpResponseStatus.OK
    private var contentType: HttpContentType = HttpContentTypes.PLAIN
    var protocolVersion: HttpVersion = HttpVersion.HTTP_1_1
    var exception: Exception? = null

    open fun withStatus(status: HttpResponseStatus) {
        this.status = status
    }

    open fun withContentType(contentType: HttpContentType) {
        this.contentType = contentType
    }

    open fun withProtocolVersion(protocolVersion: HttpVersion) {
        this.protocolVersion = protocolVersion
    }

    open fun promiseClose() {
        this.promiseClose = true
    }

    fun abortWith(errorCode: HttpResponseStatus, postHandler: () -> Unit = { }) {
        when (errorCode) {
            HttpResponseStatus.OK -> error("Error response cannot use status '200 OK'")
        }
        withStatus(errorCode)
        withContentType(HttpContentTypes.JSON)
        postHandler()
        throw EndingEarlyException().also {
            this.exception = it
        }
    }

    fun content(): ByteArray {
        return this.msg.content().let { content ->
            ByteArray(content.readableBytes()).also {
                content.readBytes(it)
            }
        }
    }

    fun stringContent(): String {
        return String(content(), StandardCharsets.UTF_8)
    }

    fun jsonContent(): JSONObject {
        return StrictJSONParser.parseObject(stringContent())
    }

    fun path(): String {
        return this.msg.uri().let {
            if (it.endsWith("/")) {
                it.substring(0, it.length - 1)
            } else{
                it
            }
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

    fun dump(): KoraContext {
        return KoraContext(this.msg).also {
            it.status = this.status
            it.contentType = this.contentType
            it.protocolVersion = this.protocolVersion
            it.promiseClose = this.promiseClose
            it.exception = this.exception
        }
    }
}