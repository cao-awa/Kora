package com.github.cao.awa.kora.server.network.http.argument.type.validator.basic

import com.github.cao.awa.kora.server.network.http.argument.type.validator.TypedHttpArgumentInitializeValidator
import com.github.cao.awa.kora.server.network.http.argument.type.validator.error

class TypedHttpArgumentBooleanInitializeValidator : TypedHttpArgumentInitializeValidator<Boolean> {
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