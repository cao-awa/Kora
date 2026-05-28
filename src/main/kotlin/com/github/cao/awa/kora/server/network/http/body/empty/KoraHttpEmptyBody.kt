package com.github.cao.awa.kora.server.network.http.body.empty

import com.github.cao.awa.kora.server.network.http.body.KoraHttpBody

object KoraHttpEmptyBody: KoraHttpBody() {
    override fun stringData(): String {
        return ""
    }
}