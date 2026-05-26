package com.github.cao.awa.kora.server.network.http.argument.type.validator

@FunctionalInterface
interface TypedHttpArgumentValidator<T : Any> {
    fun validate(value: T): T
}
