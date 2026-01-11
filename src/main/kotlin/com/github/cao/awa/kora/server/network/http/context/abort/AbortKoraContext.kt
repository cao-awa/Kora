package com.github.cao.awa.kora.server.network.http.context.abort

import com.github.cao.awa.kora.server.network.http.content.type.HttpContentType
import com.github.cao.awa.kora.server.network.http.context.KoraContext
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpResponseStatus

class AbortKoraContext(err: Exception, context: KoraContext): KoraContext(context.msg) {
    init {
        this.exception = err
        super.withStatus(context.status())
        super.withContentType(context.contentType())
        super.withProtocolVersion(context.protocolVersion())
        if (context.promiseClose) {
            promiseClose()
        }
    }

    override fun withStatus(status: HttpResponseStatus) {
        throw IllegalStateException("Cannot change response status of aborted context")
    }

    override fun withContentType(contentType: HttpContentType) {
        throw IllegalStateException("Cannot change response content type of aborted context")
    }
}