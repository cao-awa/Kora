package com.github.cao.awa.kalmia.server.network.http.context.abort

import com.github.cao.awa.kalmia.server.network.context.abort.KalmiaAbortContext
import com.github.cao.awa.kalmia.server.network.http.context.KalmiaHttpContext
import com.github.cao.awa.kalmia.server.network.http.holder.KalmiaFullHttpRequestHolder

class KalmiaAbortHttpContext(context: KalmiaHttpContext): KalmiaHttpContext(context), KalmiaAbortContext<KalmiaFullHttpRequestHolder> {
}