package com.github.cao.awa.kora.server.network.http.argument.type

import com.github.cao.awa.kora.server.network.http.argument.exception.TypedHttpArgumentMissingException
import com.github.cao.awa.kora.server.network.http.argument.type.validator.TypedHttpArgumentIntValidator
import com.github.cao.awa.kora.server.network.http.argument.type.validator.TypedHttpArgumentValidator
import com.github.cao.awa.kora.server.network.http.argument.type.validator.exception.TypedHttpArgumentValidateException
import com.github.cao.awa.kora.server.network.http.argument.type.value.TypedHttpArgumentDefaultValues
import com.github.cao.awa.kora.server.network.http.context.KoraHttpContext
import kotlin.reflect.KClass

class TypedHttpArgument<T : Any>(val name: String, private val type: KClass<T>, val missable: Boolean) {
    companion object {
        val NOTHING: TypedHttpArgument<Any> = arg<Any>("", true)
        private val validators: MutableMap<KClass<*>, TypedHttpArgumentValidator<*>> =
            mutableMapOf<KClass<*>, TypedHttpArgumentValidator<*>>().apply {
                put(Int::class, TypedHttpArgumentIntValidator())
            }
    }

    @Suppress("unchecked_cast")
    fun get(context: KoraHttpContext): T {
        val content: String = context.arguments()[this.name] ?: TypedHttpArgumentMissingException.missing("Argument '${this.name}' are missing, type is ${this.type.simpleName}")
        val validator: TypedHttpArgumentValidator<*> = validators[this.type] ?: TypedHttpArgumentValidateException.failed("Unregistered argument validator of type '${this.type}'")
        return validator.get(content) as T
    }

    operator fun invoke(context: KoraHttpContext): T {
        try {
            return get(context)
        } catch (e: Exception) {
            if (this.missable) {
                return TypedHttpArgumentDefaultValues.getDefault(this.type) as T
            } else {
                throw e
            }
        }
    }
}

inline fun <reified T : Any> arg(name: String, missable: Boolean = false): TypedHttpArgument<T> {
    return TypedHttpArgument(name, T::class, missable)
}