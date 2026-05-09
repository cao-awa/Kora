package com.github.cao.awa.kora.server.network.http.argument.type.validator

class TypedHttpArgumentShortValidator : TypedHttpArgumentValidator<Short> {
    override operator fun get(argumentName:String, content: String): Short {
        try {
            return java.lang.Short.parseShort(content)
        } catch (_: NumberFormatException) {
            error(argumentName, content, "Short")
        }
    }
}