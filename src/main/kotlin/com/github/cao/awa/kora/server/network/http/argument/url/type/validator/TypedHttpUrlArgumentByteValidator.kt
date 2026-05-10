package com.github.cao.awa.kora.server.network.http.argument.url.type.validator

class TypedHttpUrlArgumentByteValidator : TypedHttpUrlArgumentValidator<Byte> {
    override operator fun get(argumentName:String, content: String, url: String): Byte {
        try {
            return java.lang.Byte.parseByte(content)
        } catch (_: NumberFormatException) {
            error(argumentName, content, "Byte", url)
        }
    }
}