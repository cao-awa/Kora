package com.github.cao.awa.kora.server.network.http.param

import com.github.cao.awa.cason.obj.JSONObject
import com.github.cao.awa.kora.server.network.http.form.encoded.UrlEncodedForm

class HttpRequestParams {
    companion object {
        val EMPTY: HttpRequestParams = HttpRequestParams()

        fun build(data: JSONObject): HttpRequestParams {
            return HttpRequestParams().apply {
                data.forEach { (key, value) ->
                    this.data[key] = value.toString()
                }
            }
        }

        fun build(data: UrlEncodedForm): HttpRequestParams {
            return HttpRequestParams().apply {
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