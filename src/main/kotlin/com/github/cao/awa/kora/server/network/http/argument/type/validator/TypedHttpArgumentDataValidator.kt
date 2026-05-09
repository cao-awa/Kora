package com.github.cao.awa.kora.server.network.http.argument.type.validator

class TypedHttpArgumentDataValidator<T : Any>(
    private val validator: (String, String) -> T
) : TypedHttpArgumentValidator<T> {
    override operator fun get(argumentName: String, content: String): T = this.validator(argumentName, content)
}