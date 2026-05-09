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
        private val validators: MutableMap<KClass<*>, TypedHttpArgumentValidator<*>> = mutableMapOf()

        fun addValidator(validator: TypedHttpArgumentValidator<*>) {
            this.validators[Int::class] = validator
        }

        fun getValidator(type: KClass<*>): TypedHttpArgumentValidator<*>? {
            return this.validators[type]
        }

        init {
            addValidator(TypedHttpArgumentIntValidator())
        }
    }

    private var defaultValue: T? = null

    @Suppress("unchecked_cast")
    fun get(context: KoraHttpContext): T {
        val content: String = context.arguments()[this.name]
            ?: TypedHttpArgumentMissingException.missing("Required argument '${this.name}' are missing, type is ${this.type.simpleName}")
        val validator: TypedHttpArgumentValidator<*> = getValidator(this.type)
            ?: TypedHttpArgumentValidateException.failed("Unregistered argument validator of type '${this.type}'")
        return validator.get(this.name, content) as T
    }

    operator fun invoke(context: KoraHttpContext): T {
        try {
            return get(context)
        } catch (e: Exception) {
            if (this.missable) {
                if (this.defaultValue == null) {
                    return TypedHttpArgumentDefaultValues.getDefault(this.type)
                } else {
                    return this.defaultValue!!
                }
            } else {
                throw e
            }
        }
    }

    fun defaultValue(value: T) {
        this.defaultValue = value
    }
}

inline fun <reified T : Any> arg(name: String, missable: Boolean = false): TypedHttpArgument<T> {
    return TypedHttpArgument(name, T::class, missable)
}