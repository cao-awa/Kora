package com.github.cao.awa.kora.server.network.http.argument.url.type.validator

class TypedHttpUrlArgumentLongValidator : TypedHttpUrlArgumentValidator<Long> {
    override operator fun get(argumentName:String, content: String, url: String): Long {
        try {
            return java.lang.Long.parseLong(content)
        } catch (_: NumberFormatException) {
            error(argumentName, content, "Long", url)
        }
    }
}