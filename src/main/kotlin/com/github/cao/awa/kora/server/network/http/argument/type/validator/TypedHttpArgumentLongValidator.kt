package com.github.cao.awa.kora.server.network.http.argument.type.validator

import com.github.cao.awa.kora.server.network.http.argument.type.validator.exception.TypedHttpArgumentValidateException

class TypedHttpArgumentLongValidator : TypedHttpArgumentValidator<Long>() {
    override fun get(argumentName:String, content: String): Long {
        try {
            return java.lang.Long.parseLong(content)
        } catch (_: NumberFormatException) {
            throw TypedHttpArgumentValidateException("The value '$content' for the Long argument  '$argumentName'  is not valid")
        }
    }
}