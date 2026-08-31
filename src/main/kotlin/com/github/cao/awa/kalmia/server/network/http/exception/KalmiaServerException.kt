package com.github.cao.awa.kalmia.server.network.http.exception

abstract class KalmiaServerException(override val message: String? = null, override val cause: Throwable? = null): KalmiaException(message, cause) {

}