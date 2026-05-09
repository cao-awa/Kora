package com.github.cao.awa.kora.server.network.http.argument.type.validator

import com.github.cao.awa.kora.server.network.http.argument.type.validator.exception.TypedHttpArgumentValidateException

class TypedHttpArgumentShortValidator : TypedHttpArgumentValidator<Short>() {
    override fun get(argumentName:String, content: String): Short {
        try {
            return java.lang.Short.parseShort(content)
        } catch (_: NumberFormatException) {
            throw TypedHttpArgumentValidateException("The value '$content' for the Short argument  '$argumentName'  is not valid")
        }
    }
}