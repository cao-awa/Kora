package com.github.cao.awa.kalmia.server.network.http.handler.post

import com.github.cao.awa.kalmia.server.network.http.handler.KalmiaHttpRequestHandler
import io.netty.handler.codec.http.HttpMethod

class KalmiaHttpPostHandler: KalmiaHttpRequestHandler(HttpMethod.POST)