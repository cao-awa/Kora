package com.github.cao.awa.kora.server.network.http.body.form.urlencoded

import com.github.cao.awa.kora.server.network.http.body.KoraHttpBody
import com.github.cao.awa.kora.server.network.http.form.encoded.UrlEncodedForm

class KoraHttpUrlencodedBody: KoraHttpBody() {
    companion object {
        val EMPTY: KoraHttpUrlencodedBody = KoraHttpUrlencodedBody()

        fun build(data: UrlEncodedForm): KoraHttpUrlencodedBody {
            return KoraHttpUrlencodedBody().apply {
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

    override fun stringData(): String {

    }
}