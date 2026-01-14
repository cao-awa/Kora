package com.github.cao.awa.kora.server.network.http.argument

import com.github.cao.awa.cason.obj.JSONObject
import com.github.cao.awa.kora.server.network.http.form.encoded.UrlEncodedForm

class HttpRequestArguments {
    companion object {
        fun build(data: UrlEncodedForm): HttpRequestArguments {
            return HttpRequestArguments().apply {
                data.forEach { (key, value) ->
                    this.data[key] = value
                }
            }
        }
    }

    private val data: MutableMap<String, String> = mutableMapOf()

    operator fun get(key: String): String? {
        return this.data[key]
    }

    fun whenNotNull(key: String, action: (String) -> Unit) {
        get(key)?.let {
            action(it)
        }
    }
}