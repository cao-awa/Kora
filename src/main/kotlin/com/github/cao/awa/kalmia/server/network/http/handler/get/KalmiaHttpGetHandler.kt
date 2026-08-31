package com.github.cao.awa.kalmia.server.network.http.handler.get

import com.github.cao.awa.kalmia.server.network.http.handler.KalmiaHttpRequestHandler
import io.netty.handler.codec.http.HttpMethod

class KalmiaHttpGetHandler : KalmiaHttpRequestHandler(HttpMethod.GET)