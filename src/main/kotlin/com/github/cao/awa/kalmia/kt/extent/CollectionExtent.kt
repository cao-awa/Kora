package com.github.cao.awa.kalmia.kt.extent

fun <E> Collection<E>.onlyContains(element: E): Boolean {
    return this.size == 1 && this.contains(element)
}