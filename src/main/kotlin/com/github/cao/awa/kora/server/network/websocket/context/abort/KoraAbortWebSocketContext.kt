package com.github.cao.awa.kora.server.network.websocket.context.abort

import com.github.cao.awa.kora.server.network.context.abort.KoraAbortContext
import com.github.cao.awa.kora.server.network.websocket.context.KoraWebSocketContext
import com.github.cao.awa.kora.server.network.websocket.holder.KoraTextWebsocketFrameHolder

class KoraAbortWebSocketContext(context: KoraWebSocketContext): KoraWebSocketContext(context.msg, context.phase), KoraAbortContext<KoraTextWebsocketFrameHolder> {
    init {
        if (context.isPromiseClose()) {
            promiseClose()
        }
    }

    override fun promiseClose() {
        throw IllegalStateException("Cannot promise close context for aborted context")
    }
}