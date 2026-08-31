package com.github.cao.awa.kalmia.server.network.http.body.empty

import com.github.cao.awa.kalmia.server.network.http.body.KalmiaHttpBody

object KalmiaHttpEmptyBody: KalmiaHttpBody() {
    override fun stringData(): String {
        return ""
    }
}