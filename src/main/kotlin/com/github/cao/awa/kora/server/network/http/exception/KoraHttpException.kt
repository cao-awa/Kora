package com.github.cao.awa.kora.server.network.http.exception

abstract class KoraHttpException(message: String? = null, cause: Throwable? = null): RuntimeException(message, cause) {
}