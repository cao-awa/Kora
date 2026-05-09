package com.github.cao.awa.kora.server.network.http.argument.type.validator

class TypedHttpArgumentIntValidator : TypedHttpArgumentValidator<Int>() {
    override fun get(content: String): Int {
        try {
            return Integer.parseInt(content)
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("Argument '$content' is not a valid integer")
        }
    }
}