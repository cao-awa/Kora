package com.github.cao.awa.kora.server.network.http.argument.url.type.validator

class TypedHttpUrlArgumentCharValidator : TypedHttpUrlArgumentValidator<Char> {
    override operator fun get(argumentName:String, content: String, url: String): Char {
        return if(content.length == 1) {
            content[0]
        } else {
            error(argumentName, content, "Char", url)
        }
    }
}