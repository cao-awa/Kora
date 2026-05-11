package com.github.cao.awa.kora.server.network.http.placeholder.url.type.validator

class TypedHttpUrlPlaceholderLongValidator : TypedHttpUrlPlaceholderValidator<Long> {
    override operator fun get(argumentName:String, content: String, url: String): Long {
        try {
            return java.lang.Long.parseLong(content)
        } catch (_: NumberFormatException) {
            error(argumentName, content, "Long", url)
        }
    }
}