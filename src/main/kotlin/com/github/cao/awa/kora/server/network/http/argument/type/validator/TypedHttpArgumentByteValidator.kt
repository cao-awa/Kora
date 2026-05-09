package com.github.cao.awa.kora.server.network.http.argument.type.validator

class TypedHttpArgumentByteValidator : TypedHttpArgumentValidator<Byte> {
    override operator fun get(argumentName:String, content: String): Byte {
        try {
            return java.lang.Byte.parseByte(content)
        } catch (_: NumberFormatException) {
            error(argumentName, content, "Byte")
        }
    }
}