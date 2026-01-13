package com.github.cao.awa.kora.server.network.http.form.encoded

class UrlEncodedForm {
    private val form = HashMap<String, String>()

    fun add(name: String, value: String) {
     this.form[name] = value
    }

    fun remove(name: String) {
        this.form.remove(name)
    }

    fun forEach(action: (String, String) -> Unit) {
        this.form.forEach(action)
    }

    fun forEach(action: (Map.Entry<String, String>) -> Unit) {
        this.form.forEach(action)
    }
}