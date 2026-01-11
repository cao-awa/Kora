package com.github.cao.awa.kora.server.network.http.content.type

@Suppress("unused")
data class HttpContentType(val name: String, val charset: String = "utf-8") {
    override fun toString(): String {
        return "${this.name};charset=${this.charset};"
    }

    fun charset(charset: String): HttpContentType {
        return HttpContentType(this.name, charset)
    }
}