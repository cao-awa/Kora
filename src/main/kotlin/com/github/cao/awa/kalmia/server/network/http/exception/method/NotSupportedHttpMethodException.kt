package com.github.cao.awa.kalmia.server.network.http.exception.method

import com.github.cao.awa.kalmia.server.network.http.exception.KalmiaHttpException

class NotSupportedHttpMethodException(method: String): KalmiaHttpException("HTTP method not supported: $method")