package com.github.cao.awa.kora.server.network.http.argument.type.validator

import com.github.cao.awa.kora.server.network.http.argument.type.validator.exception.TypedHttpArgumentValidateException

class TypedHttpArgumentFloatValidator : TypedHttpArgumentValidator<Float>() {
    override fun get(argumentName:String, content: String): Float {
        try {
            return java.lang.Float.parseFloat(content)
        } catch (_: NumberFormatException) {
            throw TypedHttpArgumentValidateException("The value '$content' for the Float argument  '$argumentName'  is not valid")
        }
    }
}