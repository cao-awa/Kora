package com.github.cao.awa.kora.server.network.http.control.end

import com.github.cao.awa.kora.server.network.http.context.KoraContext
import com.github.cao.awa.kora.server.network.http.control.KoraException

class EndingEarlyException(context: KoraContext): KoraException(context) {
}