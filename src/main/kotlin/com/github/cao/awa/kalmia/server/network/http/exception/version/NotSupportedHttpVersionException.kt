package com.github.cao.awa.kalmia.server.network.http.exception.version

import com.github.cao.awa.kalmia.server.network.http.exception.KalmiaHttpException

class NotSupportedHttpVersionException(method: String): KalmiaHttpException("HTTP version not supported: $method")