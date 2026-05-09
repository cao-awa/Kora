package com.github.cao.awa.kora.server.network.http.argument.type.validator

class TypedHttpArgumentBooleanValidator : TypedHttpArgumentValidator<Boolean> {
    override operator fun get(argumentName: String, content: String): Boolean {
        if ("true" == content) {
            return true
        } else if ("false" == content) {
            return false
        } else {
            error(argumentName, content, "Boolean")
        }
    }
}