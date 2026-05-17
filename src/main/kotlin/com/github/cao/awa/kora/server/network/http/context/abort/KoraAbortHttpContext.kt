package com.github.cao.awa.kora.server.network.http.context.abort

import com.github.cao.awa.kora.server.network.context.abort.KoraAbortContext
import com.github.cao.awa.kora.server.network.http.context.KoraHttpContext
import com.github.cao.awa.kora.server.network.http.holder.KoraFullHttpRequestHolder

class KoraAbortHttpContext(context: KoraHttpContext): KoraHttpContext(context), KoraAbortContext<KoraFullHttpRequestHolder> {
}