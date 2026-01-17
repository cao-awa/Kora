package com.github.cao.awa.kora.server.network.http.argument.type.validator

class TypedHttpArgumentIntValidator : TypedHttpArgumentValidator<Int>() {
    override fun get(content: String): Int {
        return Integer.parseInt(content)
    }
}