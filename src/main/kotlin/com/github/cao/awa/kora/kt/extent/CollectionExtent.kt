package com.github.cao.awa.kora.kt.extent

fun <E> Collection<E>.onlyContains(element: E): Boolean {
    return this.size == 1 && this.contains(element)
}