package com.github.cao.awa.kora.server.network.http.argument.type.validator

class TypedHttpArgumentIntValidator : TypedHttpArgumentValidator<Int> {
    override operator fun get(argumentName:String, content: String): Int {
        try {
            return Integer.parseInt(content)
        } catch (_: NumberFormatException) {
            error(argumentName, content, "Int")
        }
    }
}