package com.github.cao.awa.kalmia.server.network.http.placeholder.url.type.combinator

@FunctionalInterface
interface TypedHttpUrlPlaceholderCombinator<T : Any> {
    fun combinate(value: T): T
}