package com.github.cao.awa.kora.server.network.http.argument.type.validator

class TypedHttpArgumentCharValidator : TypedHttpArgumentValidator<Char> {
    override operator fun get(argumentName:String, content: String): Char {
        return if(content.length == 1) {
            content[0]
        } else {
            error(argumentName, content, "Char")
        }
    }
}