package com.github.cao.awa.kora.server.network.http.argument.type.validator.basic

import com.github.cao.awa.kora.server.network.http.argument.type.validator.TypedHttpArgumentInitializeValidator
import com.github.cao.awa.kora.server.network.http.argument.type.validator.error

class TypedHttpArgumentDoubleInitializeValidator : TypedHttpArgumentInitializeValidator<Double> {
    override operator fun get(argumentName:String, content: String): Double {
        try {
            return java.lang.Double.parseDouble(content)
        } catch (_: NumberFormatException) {
            error(argumentName, content, "Double")
        }
    }
}