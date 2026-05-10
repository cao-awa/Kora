package com.github.cao.awa.kora.server.network.http.argument.url.type.validator

class TypedHttpUrlArgumentFloatValidator : TypedHttpUrlArgumentValidator<Float> {
    override operator fun get(argumentName:String, content: String, url: String): Float {
        try {
            return java.lang.Float.parseFloat(content)
        } catch (_: NumberFormatException) {
            error(argumentName, content, "Float", url)
        }
    }
}