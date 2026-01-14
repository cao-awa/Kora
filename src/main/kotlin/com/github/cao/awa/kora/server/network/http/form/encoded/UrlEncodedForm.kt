package com.github.cao.awa.kora.server.network.http.form.encoded

class UrlEncodedForm {
    companion object {
        val EMPTY = UrlEncodedForm()

        fun build(content: String): UrlEncodedForm {
            return UrlEncodedForm().apply {
                if (content.isNotEmpty()) {
                    content.split("&").forEach {
                        this.form[it.substringBefore("=")] = it.substringAfter("=")
                    }
                }
            }
        }
    }
    private val form = HashMap<String, String>()

    fun get(key: String): String? {
        return this.form[key]
    }

    fun forEach(action: (String, String) -> Unit) {
        this.form.forEach(action)
    }

    fun forEach(action: (Map.Entry<String, String>) -> Unit) {
        this.form.forEach(action)
    }
}