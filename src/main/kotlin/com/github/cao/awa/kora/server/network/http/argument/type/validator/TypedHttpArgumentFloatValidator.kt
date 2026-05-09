package com.github.cao.awa.kora.server.network.http.argument.type.validator

class TypedHttpArgumentFloatValidator : TypedHttpArgumentValidator<Float> {
    override operator fun get(argumentName:String, content: String): Float {
        try {
            return java.lang.Float.parseFloat(content)
        } catch (_: NumberFormatException) {
            error(argumentName, content, "Float")
        }
    }
}