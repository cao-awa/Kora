package com.github.cao.awa.kalmia.server.network.http.argument.type.combinator

@FunctionalInterface
interface TypedHttpArgumentCombinator<T : Any> {
    fun combinate(value: T): T
}