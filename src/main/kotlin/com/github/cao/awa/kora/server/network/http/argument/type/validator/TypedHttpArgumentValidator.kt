package com.github.cao.awa.kora.server.network.http.argument.type.validator

abstract class TypedHttpArgumentValidator<T: Any> {
    abstract fun get(content: String): T
}