package com.github.cao.awa.kora.server.network.http.exception

import com.github.cao.awa.kora.server.network.http.control.KoraException
import io.netty.handler.codec.http.HttpMethod

class NotSupportedMethodException(method: HttpMethod): KoraException("HTTP Method not supported: ${method.name()}") {
}