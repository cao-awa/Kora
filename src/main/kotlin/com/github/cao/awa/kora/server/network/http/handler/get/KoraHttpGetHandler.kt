package com.github.cao.awa.kora.server.network.http.handler.get

import com.github.cao.awa.kora.server.network.http.handler.KoraHttpRequestHandler
import io.netty.handler.codec.http.HttpMethod

class KoraHttpGetHandler : KoraHttpRequestHandler(HttpMethod.GET)