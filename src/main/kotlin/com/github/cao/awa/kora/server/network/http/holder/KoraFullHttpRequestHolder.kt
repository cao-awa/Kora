package com.github.cao.awa.kora.server.network.http.holder

import com.github.cao.awa.kora.server.network.holder.PathByteBufHolder
import io.netty.handler.codec.http.*
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class KoraFullHttpRequestHolder(val msg: FullHttpRequest): PathByteBufHolder(
    msg,
    URLDecoder.decode(msg.uri(), StandardCharsets.UTF_8)
) {
    fun method(): HttpMethod = this.msg.method()

    fun protocolVersion(): HttpVersion = this.msg.protocolVersion()

    fun headers(): HttpHeaders = this.msg.headers()
}