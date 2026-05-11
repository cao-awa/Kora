package com.github.cao.awa.kora.server.network.http.placeholder.url.type.validator

class TypedHttpUrlPlaceholderCharValidator : TypedHttpUrlPlaceholderValidator<Char> {
    override operator fun get(argumentName:String, content: String, url: String): Char {
        return if(content.length == 1) {
            content[0]
        } else {
            error(argumentName, content, "Char", url)
        }
    }
}