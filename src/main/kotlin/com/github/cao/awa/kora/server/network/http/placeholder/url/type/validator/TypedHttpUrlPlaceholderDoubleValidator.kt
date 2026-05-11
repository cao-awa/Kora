package com.github.cao.awa.kora.server.network.http.placeholder.url.type.validator

class TypedHttpUrlPlaceholderDoubleValidator : TypedHttpUrlPlaceholderValidator<Double> {
    override operator fun get(argumentName:String, content: String, url: String): Double {
        try {
            return java.lang.Double.parseDouble(content)
        } catch (_: NumberFormatException) {
            error(argumentName, content, "Double", url)
        }
    }
}