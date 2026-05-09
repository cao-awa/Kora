package com.github.cao.awa.kora.server.network.http.argument.type.validator

import com.github.cao.awa.kora.server.network.http.argument.type.validator.exception.TypedHttpArgumentValidateException

class TypedHttpArgumentByteValidator : TypedHttpArgumentValidator<Byte>() {
    override fun get(argumentName:String, content: String): Byte {
        try {
            return java.lang.Byte.parseByte(content)
        } catch (_: NumberFormatException) {
            throw TypedHttpArgumentValidateException("The value '$content' for the Byte argument  '$argumentName'  is not valid")
        }
    }
}