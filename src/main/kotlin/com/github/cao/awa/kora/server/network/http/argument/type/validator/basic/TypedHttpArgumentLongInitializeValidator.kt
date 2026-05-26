package com.github.cao.awa.kora.server.network.http.argument.type.validator.basic

import com.github.cao.awa.kora.server.network.http.argument.type.validator.TypedHttpArgumentInitializeValidator
import com.github.cao.awa.kora.server.network.http.argument.type.validator.error

class TypedHttpArgumentLongInitializeValidator : TypedHttpArgumentInitializeValidator<Long> {
    override operator fun get(argumentName:String, content: String): Long {
        try {
            return java.lang.Long.parseLong(content)
        } catch (_: NumberFormatException) {
            error(argumentName, content, "Long")
        }
    }
}