package com.github.cao.awa.kora.server.network.http.path.exception

import com.github.cao.awa.kora.server.network.http.exception.KoraPathException
import com.github.cao.awa.kora.server.network.http.exception.KoraServerException

class HttpPathNotRegisteredException(val msg: String): KoraServerException(msg) {
}