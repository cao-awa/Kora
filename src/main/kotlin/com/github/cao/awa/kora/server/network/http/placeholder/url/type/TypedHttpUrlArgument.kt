package com.github.cao.awa.kora.server.network.http.placeholder.url.type

import com.github.cao.awa.kora.server.network.http.argument.exception.TypedHttpArgumentMissingException
import com.github.cao.awa.kora.server.network.http.argument.type.validator.exception.TypedHttpArgumentValidateException
import com.github.cao.awa.kora.server.network.http.placeholder.url.type.validator.TypedHttpUrlArgumentBooleanValidator
import com.github.cao.awa.kora.server.network.http.placeholder.url.type.validator.TypedHttpUrlArgumentByteValidator
import com.github.cao.awa.kora.server.network.http.placeholder.url.type.validator.TypedHttpUrlArgumentCharValidator
import com.github.cao.awa.kora.server.network.http.placeholder.url.type.validator.TypedHttpUrlArgumentDoubleValidator
import com.github.cao.awa.kora.server.network.http.placeholder.url.type.validator.TypedHttpUrlArgumentFloatValidator
import com.github.cao.awa.kora.server.network.http.placeholder.url.type.validator.TypedHttpUrlArgumentIntValidator
import com.github.cao.awa.kora.server.network.http.placeholder.url.type.validator.TypedHttpUrlArgumentLongValidator
import com.github.cao.awa.kora.server.network.http.placeholder.url.type.validator.TypedHttpUrlArgumentShortValidator
import com.github.cao.awa.kora.server.network.http.placeholder.url.type.validator.TypedHttpUrlArgumentStringValidator
import com.github.cao.awa.kora.server.network.http.placeholder.url.type.validator.TypedHttpUrlArgumentValidator
import com.github.cao.awa.kora.server.network.http.context.KoraHttpContext
import com.github.cao.awa.kora.server.network.http.placeholder.url.exception.TypedHttpUrlMissingException
import kotlin.reflect.KClass

class TypedHttpUrlArgument<T : Any>(val name: String, private val type: KClass<T>) {
    companion object {
        private val validators: MutableMap<KClass<*>, TypedHttpUrlArgumentValidator<*>> = mutableMapOf()

        private fun <T : Any> addValidator(type: KClass<T>, validator: TypedHttpUrlArgumentValidator<T>) {
            this.validators[type] = validator
        }

        @Suppress("unchecked_cast")
        fun <T : Any> getValidator(type: KClass<T>): TypedHttpUrlArgumentValidator<T>? {
            return this.validators[type] as TypedHttpUrlArgumentValidator<T>?
        }

        init {
            addValidator(Short::class, TypedHttpUrlArgumentShortValidator())
            addValidator(Int::class, TypedHttpUrlArgumentIntValidator())
            addValidator(Long::class, TypedHttpUrlArgumentLongValidator())
            addValidator(Float::class, TypedHttpUrlArgumentFloatValidator())
            addValidator(Double::class, TypedHttpUrlArgumentDoubleValidator())

            addValidator(Boolean::class, TypedHttpUrlArgumentBooleanValidator())

            addValidator(Byte::class, TypedHttpUrlArgumentByteValidator())
            addValidator(Char::class, TypedHttpUrlArgumentCharValidator())

            addValidator(String::class, TypedHttpUrlArgumentStringValidator())
        }
    }

    @Suppress("unchecked_cast")
    fun get(context: KoraHttpContext): T {
        val contentList = context.path().split("/")
        val seq: Int = context.placeholders()[this.name] ?: missing(context)

        if (contentList.size <= seq) {
            missing(context)
        }

        val validator: TypedHttpUrlArgumentValidator<*> = getValidator(this.type)
            ?: TypedHttpArgumentValidateException.failed("Unregistered placeholder validator of type '${this.type}'")
        val content = contentList[seq]
        return validator[this.name, content, context.path()] as T
    }

    private fun missing(context: KoraHttpContext): Nothing = TypedHttpUrlMissingException.missing(
        "Required placeholder '${this.name}' is missing, type is ${this.type.simpleName}, at '${context.placeholderURL()}'"
    )

    operator fun invoke(context: KoraHttpContext): T {
        return get(context)
    }
}

inline fun <reified T : Any> urlArg(name: String): TypedHttpUrlArgument<T> {
    return TypedHttpUrlArgument(name, T::class)
}