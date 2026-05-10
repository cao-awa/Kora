package com.github.cao.awa.kora.server.network.http.exception.version

import com.github.cao.awa.kora.server.network.http.exception.KoraPathException

class NotSupportedHttpVersionException(method: String): KoraPathException("HTTP version not supported: $method")