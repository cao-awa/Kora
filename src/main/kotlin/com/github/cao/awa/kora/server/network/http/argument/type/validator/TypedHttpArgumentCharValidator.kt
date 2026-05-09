package com.github.cao.awa.kora.server.network.http.argument.type.validator

import com.github.cao.awa.kora.server.network.http.argument.type.validator.exception.TypedHttpArgumentValidateException

class TypedHttpArgumentCharValidator : TypedHttpArgumentValidator<Char>() {
    override fun get(argumentName:String, content: String): Char {
        return if(content.length == 1) {
            content[0]
        } else {
            throw TypedHttpArgumentValidateException("The value '$content' for the Char argument  '$argumentName'  is not valid")
        }
    }
}