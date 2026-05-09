package com.github.cao.awa.kora.server.network.http.argument.type.validator

import com.github.cao.awa.kora.server.network.http.argument.type.validator.exception.TypedHttpArgumentValidateException

class TypedHttpArgumentDoubleValidator : TypedHttpArgumentValidator<Double>() {
    override fun get(argumentName:String, content: String): Double {
        try {
            return java.lang.Double.parseDouble(content)
        } catch (_: NumberFormatException) {
            throw TypedHttpArgumentValidateException("The value '$content' for the Double argument  '$argumentName'  is not valid")
        }
    }
}