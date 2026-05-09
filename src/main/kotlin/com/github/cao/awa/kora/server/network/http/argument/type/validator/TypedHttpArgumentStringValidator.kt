package com.github.cao.awa.kora.server.network.http.argument.type.validator

class TypedHttpArgumentStringValidator : TypedHttpArgumentValidator<String> {
    override operator fun get(argumentName:String, content: String): String = content
}