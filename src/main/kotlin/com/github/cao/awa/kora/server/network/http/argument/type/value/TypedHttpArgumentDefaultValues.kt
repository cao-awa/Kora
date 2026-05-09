package com.github.cao.awa.kora.server.network.http.argument.type.value

import kotlin.reflect.KClass

object TypedHttpArgumentDefaultValues {
    val defaultValues: MutableMap<KClass<*>, Any> = HashMap<KClass<*>, Any>().also {
        it[Boolean::class] = false
        it[Byte::class] = 0
        it[Short::class] = 0
        it[Char::class] = '\u0000'
        it[Int::class] = 0
        it[Long::class] = 0
        it[Float::class] = 0F
        it[Double::class] = 0.0
        it[String::class] = ""
    }

    @Suppress("unchecked_cast")
    fun <T : Any> getDefault(type: KClass<T>): T  {
        return (defaultValues[type] ?: error("The argument type ${type.simpleName} doesn't have a default value, must input a value")) as T
    }
}