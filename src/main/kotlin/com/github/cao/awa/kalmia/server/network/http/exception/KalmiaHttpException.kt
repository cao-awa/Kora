package com.github.cao.awa.kalmia.server.network.http.exception

abstract class KalmiaHttpException(override val message: String? = null, override val cause: Throwable? = null): KalmiaException(message, cause) {
}