package com.github.cao.awa.kora.server.network.http.exception

abstract class KoraServerException(override val message: String? = null, override val cause: Throwable? = null): KoraException(message, cause) {

}