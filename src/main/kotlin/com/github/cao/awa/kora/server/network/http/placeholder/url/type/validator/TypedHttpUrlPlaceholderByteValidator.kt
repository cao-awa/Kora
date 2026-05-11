package com.github.cao.awa.kora.server.network.http.placeholder.url.type.validator

class TypedHttpUrlPlaceholderByteValidator : TypedHttpUrlPlaceholderValidator<Byte> {
    override operator fun get(argumentName:String, content: String, url: String): Byte {
        try {
            return java.lang.Byte.parseByte(content)
        } catch (_: NumberFormatException) {
            error(argumentName, content, "Byte", url)
        }
    }
}