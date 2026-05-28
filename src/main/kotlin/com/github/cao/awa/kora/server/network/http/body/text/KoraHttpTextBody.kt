package com.github.cao.awa.kora.server.network.http.body.text

import com.github.cao.awa.kora.server.network.http.body.KoraHttpBody

class KoraHttpTextBody(val text: String): KoraHttpBody() {
    override fun stringData(): String {
        return this.text
    }
}