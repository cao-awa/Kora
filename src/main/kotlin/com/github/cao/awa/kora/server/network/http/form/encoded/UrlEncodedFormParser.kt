package com.github.cao.awa.kora.server.network.http.form.encoded

object UrlEncodedFormParser {
    fun parse(content: String): UrlEncodedForm {
        return UrlEncodedForm().apply {
            if (content.isNotEmpty()) {
                content.split("&").forEach {
                    add(it.substringBefore("=").trim(), it.substringAfter("=").trim())
                }
            }
        }
    }
}