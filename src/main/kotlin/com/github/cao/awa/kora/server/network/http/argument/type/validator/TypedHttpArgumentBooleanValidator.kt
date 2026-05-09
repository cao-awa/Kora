package com.github.cao.awa.kora.server.network.http.argument.type.validator

import com.github.cao.awa.kora.server.network.http.argument.type.validator.exception.TypedHttpArgumentValidateException

class TypedHttpArgumentBooleanValidator : TypedHttpArgumentValidator<Boolean>() {
    override fun get(argumentName:String, content: String): Boolean {
        if ("true" == content){
            return true
        } else if ("false" == content){
            return false
        } else {
            throw TypedHttpArgumentValidateException("The value '$content' for the Boolean argument  '$argumentName'  is not valid")
        }
    }
}