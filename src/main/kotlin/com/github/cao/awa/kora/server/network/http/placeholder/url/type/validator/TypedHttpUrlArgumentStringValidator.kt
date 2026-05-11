package com.github.cao.awa.kora.server.network.http.placeholder.url.type.validator

class TypedHttpUrlArgumentStringValidator : TypedHttpUrlArgumentValidator<String> {
    override operator fun get(argumentName:String, content: String, url: String): String = content
}