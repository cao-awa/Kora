package com.github.cao.awa.kora.server.network.http.argument.type.validator

import com.github.cao.awa.kora.server.network.http.argument.type.validator.exception.TypedHttpArgumentValidateException

class TypedHttpArgumentIntValidator : TypedHttpArgumentValidator<Int>() {
    override fun get(argumentName:String, content: String): Int {
        try {
            return Integer.parseInt(content)
        } catch (e: NumberFormatException) {
            throw TypedHttpArgumentValidateException("The value '$content' for the Int argument  '$argumentName'  is not valid")
        }
    }
}