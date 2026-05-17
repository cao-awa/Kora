package com.github.cao.awa.kora.server.network.http.exception.version

import com.github.cao.awa.kora.server.network.http.exception.KoraHttpException

class NotSupportedHttpVersionException(method: String): KoraHttpException("HTTP version not supported: $method")