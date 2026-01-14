package com.github.cao.awa.kora.server.network.http.exception

import io.netty.handler.codec.http.HttpMethod

class NotSupportedHttpMethodException(method: HttpMethod): KoraHttpException("HTTP Method not supported: ${method.name()}") {
}