package com.github.cao.awa.kora.server.network.http.argument.url.type.validator

class TypedHttpUrlArgumentBooleanValidator : TypedHttpUrlArgumentValidator<Boolean> {
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