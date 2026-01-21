package com.github.cao.awa.kora.server.network.ws.context

import com.github.cao.awa.kora.server.network.context.KoraContext
import com.github.cao.awa.kora.server.network.exception.abort.EndingEarlyException
import com.github.cao.awa.kora.server.network.ws.context.abort.KoraAbortWebSocketContext
import com.github.cao.awa.kora.server.network.ws.holder.KoraTextWebsocketFrameHolder
import com.github.cao.awa.kora.server.network.ws.phase.KoraWebSocketPhase

@Suppress("unused")
open class KoraWebSocketContext(val msg: KoraTextWebsocketFrameHolder, val phase: KoraWebSocketPhase): KoraContext<KoraTextWebsocketFrameHolder, KoraWebSocketContext, KoraAbortWebSocketContext>(msg) {
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
        return KoraWebSocketContext(this.msg, this.phase).also {
            it.promiseClose = this.promiseClose
        }
    }

    override fun createAbort(): KoraAbortWebSocketContext {
        return KoraAbortWebSocketContext(this)
    }
}