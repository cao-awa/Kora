package com.github.cao.awa.kora.server.network.http.exception.method

import com.github.cao.awa.kora.server.network.http.exception.KoraPathException

class NotSupportedHttpMethodException(method: String): KoraPathException("HTTP method not supported: $method")