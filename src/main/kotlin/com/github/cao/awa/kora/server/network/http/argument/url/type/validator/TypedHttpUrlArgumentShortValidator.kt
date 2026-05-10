package com.github.cao.awa.kora.server.network.http.argument.url.type.validator

class TypedHttpUrlArgumentShortValidator : TypedHttpUrlArgumentValidator<Short> {
    override operator fun get(argumentName:String, content: String, url: String): Short {
        try {
            return java.lang.Short.parseShort(content)
        } catch (_: NumberFormatException) {
            error(argumentName, content, "Short", url)
        }
    }
}