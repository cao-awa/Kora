package com.github.cao.awa.kora.server.network.http.argument.type

import com.github.cao.awa.kora.server.network.http.argument.type.validator.TypedHttpArgumentIntValidator
import com.github.cao.awa.kora.server.network.http.argument.type.validator.TypedHttpArgumentValidator
import com.github.cao.awa.kora.server.network.http.context.KoraHttpContext
import kotlin.reflect.KClass

class TypedHttpArgument<T : Any>(val name: String, private val type: KClass<T>) {
    companion object {
        val NOTHING: TypedHttpArgument<Any> = arg<Any>("")
        private val validators: MutableMap<KClass<*>, TypedHttpArgumentValidator<*>> =
            mutableMapOf<KClass<*>, TypedHttpArgumentValidator<*>>().apply {
                put(Int::class, TypedHttpArgumentIntValidator())
            }
    }

    @Suppress("unchecked_cast")
    fun get(context: KoraHttpContext): T {
        val content: String = context.arguments()[this.name] ?: error("Argument ${this.name} are missing")
        val validator: TypedHttpArgumentValidator<*> = validators[this.type] ?: error("Unregistered argument validator of type '${this.type}'")
        return validator.get(content) as T
    }

    operator fun invoke(context: KoraHttpContext): T {
        return get(context)
    }
}

inline fun <reified T : Any> arg(name: String): TypedHttpArgument<T> {
    return TypedHttpArgument(name, T::class)
}