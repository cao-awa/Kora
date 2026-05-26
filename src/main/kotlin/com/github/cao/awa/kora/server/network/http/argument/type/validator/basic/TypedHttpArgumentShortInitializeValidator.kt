package com.github.cao.awa.kora.server.network.http.argument.type.validator.basic

import com.github.cao.awa.kora.server.network.http.argument.type.validator.TypedHttpArgumentInitializeValidator
import com.github.cao.awa.kora.server.network.http.argument.type.validator.error

class TypedHttpArgumentShortInitializeValidator : TypedHttpArgumentInitializeValidator<Short> {
    override operator fun get(argumentName:String, content: String): Short {
        try {
            return java.lang.Short.parseShort(content)
        } catch (_: NumberFormatException) {
            error(argumentName, content, "Short")
        }
    }
}