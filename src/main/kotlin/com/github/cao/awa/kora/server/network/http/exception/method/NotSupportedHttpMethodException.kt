package com.github.cao.awa.kora.server.network.http.exception.method

import com.github.cao.awa.kora.server.network.http.exception.KoraHttpException

class NotSupportedHttpMethodException(method: String): KoraHttpException("HTTP method not supported: $method")