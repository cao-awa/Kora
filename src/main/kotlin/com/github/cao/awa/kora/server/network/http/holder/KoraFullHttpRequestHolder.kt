package com.github.cao.awa.kora.server.network.http.holder

import com.github.cao.awa.kora.server.network.holder.PathByteBufHolder
import io.netty.handler.codec.http.*

class KoraFullHttpRequestHolder(val msg: FullHttpRequest): PathByteBufHolder(msg, msg.uri()) {
    fun method(): HttpMethod = this.msg.method()

    fun protocolVersion(): HttpVersion = this.msg.protocolVersion()

    fun headers(): HttpHeaders = this.msg.headers()
}