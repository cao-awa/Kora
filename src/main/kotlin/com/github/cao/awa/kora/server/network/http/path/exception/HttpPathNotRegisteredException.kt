package com.github.cao.awa.kora.server.network.http.path.exception

import com.github.cao.awa.kora.server.network.http.exception.KoraHttpException

class HttpPathNotRegisteredException(val msg: String): KoraHttpException(msg) {
}