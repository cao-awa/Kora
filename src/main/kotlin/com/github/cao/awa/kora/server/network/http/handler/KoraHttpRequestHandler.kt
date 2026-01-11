package com.github.cao.awa.kora.server.network.http.handler

import com.github.cao.awa.cason.obj.JSONObject
import com.github.cao.awa.kora.server.network.response.KoraContext

class KoraHttpRequestHandler {
    fun handleFull(context: KoraContext) {
        println(context.jsonContent())

        context.responseJSON {
            promiseClose = true

            JSONObject {
                "test" set "value"
                "id" set System.currentTimeMillis()
            }
        }
    }
}