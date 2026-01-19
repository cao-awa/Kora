package com.github.cao.awa.kora.server.network.ws.context.abort

import com.github.cao.awa.kora.server.network.context.abort.KoraAbortContext
import com.github.cao.awa.kora.server.network.http.content.type.HttpContentType
import com.github.cao.awa.kora.server.network.http.context.KoraHttpContext
import com.github.cao.awa.kora.server.network.http.holder.KoraFullHttpRequestHolder
import com.github.cao.awa.kora.server.network.ws.context.KoraWebSocketContext
import com.github.cao.awa.kora.server.network.ws.holder.KoraTextWebsocketFrameHolder
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpVersion

class KoraAbortWebSocketContext(context: KoraWebSocketContext): KoraWebSocketContext(context.msg), KoraAbortContext<KoraTextWebsocketFrameHolder> {
    init {
        if (context.isPromiseClose()) {
            promiseClose()
        }
    }

    override fun promiseClose() {
        throw IllegalStateException("Cannot promise close context for aborted context")
    }
}