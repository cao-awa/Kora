package com.github.cao.awa.kora.server.network.http.argument.type.validator

class TypedHttpArgumentLongValidator : TypedHttpArgumentValidator<Long> {
    override operator fun get(argumentName:String, content: String): Long {
        try {
            return java.lang.Long.parseLong(content)
        } catch (_: NumberFormatException) {
            error(argumentName, content, "Long")
        }
    }
}