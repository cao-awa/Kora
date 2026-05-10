package com.github.cao.awa.kora.server.network.http.exception

abstract class KoraHttpException(override val message: String? = null, override val cause: Throwable? = null): RuntimeException(message, cause) {
}