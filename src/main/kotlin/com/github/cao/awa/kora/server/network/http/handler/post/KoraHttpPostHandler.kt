package com.github.cao.awa.kora.server.network.http.handler.post

import com.github.cao.awa.kora.server.network.http.handler.KoraHttpRequestHandler
import io.netty.handler.codec.http.HttpMethod

class KoraHttpPostHandler: KoraHttpRequestHandler(HttpMethod.POST)