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
    var status: HttpResponseStatus = HttpResponseStatus.OK
    var contentType: HttpContentType = HttpContentTypes.PLAIN
    var protocolVersion: HttpVersion = HttpVersion.HTTP_1_1
    var exception: Exception? = null

    fun withStatus(status: HttpResponseStatus) {
        this.status = status
    }

    fun withContentType(contentType: HttpContentType) {
        this.contentType = contentType
    }

    fun withProtocolVersion(protocolVersion: HttpVersion) {
        this.protocolVersion = protocolVersion
    }

    fun promiseClose() {
        this.promiseClose = true
    }

    fun abortWith(errorCode: HttpResponseStatus, postHandler: () -> Unit) {
        when (errorCode) {
            HttpResponseStatus.OK -> error("Error response cannot use status '200 OK'")
        }
        withStatus(errorCode)
        postHandler()
        throw EndingEarlyException(this).also {
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

    fun method(): HttpMethod {
        return this.msg.method()
    }

    fun protocolVersion(): HttpVersion {
        return this.msg.protocolVersion()
    }
}