package com.github.cao.awa.kora.server.network.http.placeholder.url.type.validator

@FunctionalInterface
interface TypedHttpUrlPlaceholderValidator<T : Any> {
    fun validate(value: T): T
}
