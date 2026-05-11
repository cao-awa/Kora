package com.github.cao.awa.kora.server.network.http.placeholder.url.type.validator

class TypedHttpUrlPlaceholderBooleanValidator : TypedHttpUrlPlaceholderValidator<Boolean> {
    override operator fun get(argumentName:String, content: String, url: String): Boolean {
        if ("true" == content) {
            return true
        } else if ("false" == content) {
            return false
        } else {
            error(argumentName, content, "Boolean", url)
        }
    }
}