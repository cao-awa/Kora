package com.github.cao.awa.kora.server.network.http.context.abort

import com.github.cao.awa.kora.server.network.http.context.KoraContext
import io.netty.handler.codec.http.FullHttpRequest

class AbortKoraContext(err: Exception, msg: FullHttpRequest): KoraContext(msg) {
    init {
        this.exception = err
    }
}