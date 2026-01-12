package com.github.cao.awa.kora.server.network.http.control

abstract class KoraException(message: String? = null, cause: Throwable? = null): RuntimeException(message, cause) {
}