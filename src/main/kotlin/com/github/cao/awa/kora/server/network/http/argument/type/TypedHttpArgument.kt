package com.github.cao.awa.kora.server.network.http.argument.type

import com.github.cao.awa.kora.server.network.http.argument.exception.TypedHttpArgumentMissingException
import com.github.cao.awa.kora.server.network.http.argument.type.validator.TypedHttpArgumentBooleanValidator
import com.github.cao.awa.kora.server.network.http.argument.type.validator.TypedHttpArgumentByteValidator
import com.github.cao.awa.kora.server.network.http.argument.type.validator.TypedHttpArgumentCharValidator
import com.github.cao.awa.kora.server.network.http.argument.type.validator.TypedHttpArgumentDataValidator
import com.github.cao.awa.kora.server.network.http.argument.type.validator.TypedHttpArgumentDoubleValidator
import com.github.cao.awa.kora.server.network.http.argument.type.validator.TypedHttpArgumentFloatValidator
import com.github.cao.awa.kora.server.network.http.argument.type.validator.TypedHttpArgumentIntValidator
import com.github.cao.awa.kora.server.network.http.argument.type.validator.TypedHttpArgumentJSONArrayValidator
import com.github.cao.awa.kora.server.network.http.argument.type.validator.TypedHttpArgumentLongValidator
import com.github.cao.awa.kora.server.network.http.argument.type.validator.TypedHttpArgumentShortValidator
import com.github.cao.awa.kora.server.network.http.argument.type.validator.TypedHttpArgumentStringValidator
import com.github.cao.awa.kora.server.network.http.argument.type.validator.TypedHttpArgumentValidator
import com.github.cao.awa.kora.server.network.http.argument.type.validator.error
import com.github.cao.awa.kora.server.network.http.argument.type.validator.exception.TypedHttpArgumentValidateException
import com.github.cao.awa.kora.server.network.http.argument.type.value.TypedHttpArgumentDefaultValues
import com.github.cao.awa.kora.server.network.http.context.KoraHttpContext
import kotlin.reflect.KClass

class TypedHttpArgument<T : Any>(val name: String, private val type: KClass<T>, val missable: Boolean) {
    companion object {
        private val validators: MutableMap<KClass<*>, TypedHttpArgumentValidator<*>> = mutableMapOf()

        fun <T: Any> addValidator(type: KClass<T>, validator: TypedHttpArgumentValidator<T>) {
            this.validators[type] = validator
        }

        fun <T: Any> addValidator(type: KClass<T>, validator: (String, String) -> T) {
            this.validators[type] = TypedHttpArgumentDataValidator(validator)
        }

        @Suppress("unchecked_cast")
        fun<T:Any> getValidator(type: KClass<T>): TypedHttpArgumentValidator<T>? {
            return this.validators[type] as TypedHttpArgumentValidator<T>?
        }

        init {
            addValidator(Short::class, TypedHttpArgumentShortValidator())
            addValidator(Int::class, TypedHttpArgumentIntValidator())
            addValidator(Long::class, TypedHttpArgumentLongValidator())
            addValidator(Float::class, TypedHttpArgumentFloatValidator())
            addValidator(Double::class, TypedHttpArgumentDoubleValidator())

            addValidator(Boolean::class, TypedHttpArgumentBooleanValidator())

            addValidator(Byte::class, TypedHttpArgumentByteValidator())
            addValidator(Char::class, TypedHttpArgumentCharValidator())

            addValidator(String::class, TypedHttpArgumentStringValidator())
        }
    }

    private var defaultValue: T? = null

    @Suppress("unchecked_cast")
    fun get(context: KoraHttpContext): T {
        val content: String = context.arguments()[this.name]
            ?: TypedHttpArgumentMissingException.missing("Required argument '${this.name}' is missing, type is ${this.type.simpleName}")
        val validator: TypedHttpArgumentValidator<*> = getValidator(this.type)
            ?: TypedHttpArgumentValidateException.failed("Unregistered argument validator of type '${this.type}'")
        return validator[this.name, content] as T
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