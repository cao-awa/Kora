package com.github.cao.awa.kora.server.network.http.body.json

import com.github.cao.awa.cason.obj.JSONObject
import com.github.cao.awa.kora.server.network.http.body.KoraHttpBody

class KoraHttpJsonBody(val json: JSONObject): KoraHttpBody() {
    override fun stringData(): String {
        return this.json.toString()
    }
}