package com.github.cao.awa.kora.server.network.http.argument.url.type.validator

class TypedHttpUrlArgumentDoubleValidator : TypedHttpUrlArgumentValidator<Double> {
    override operator fun get(argumentName:String, content: String, url: String): Double {
        try {
            return java.lang.Double.parseDouble(content)
        } catch (_: NumberFormatException) {
            error(argumentName, content, "Double", url)
        }
    }
}