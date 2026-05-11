package com.github.cao.awa.kora.server.network.http.placeholder.url.type

import com.github.cao.awa.kora.server.network.http.argument.type.validator.exception.TypedHttpArgumentValidateException
import com.github.cao.awa.kora.server.network.http.placeholder.url.type.validator.TypedHttpUrlPlaceholderBooleanValidator
import com.github.cao.awa.kora.server.network.http.placeholder.url.type.validator.TypedHttpUrlPlaceholderByteValidator
import com.github.cao.awa.kora.server.network.http.placeholder.url.type.validator.TypedHttpUrlPlaceholderCharValidator
import com.github.cao.awa.kora.server.network.http.placeholder.url.type.validator.TypedHttpUrlPlaceholderDoubleValidator
import com.github.cao.awa.kora.server.network.http.placeholder.url.type.validator.TypedHttpUrlPlaceholderFloatValidator
import com.github.cao.awa.kora.server.network.http.placeholder.url.type.validator.TypedHttpUrlPlaceholderIntValidator
import com.github.cao.awa.kora.server.network.http.placeholder.url.type.validator.TypedHttpUrlPlaceholderLongValidator
import com.github.cao.awa.kora.server.network.http.placeholder.url.type.validator.TypedHttpUrlPlaceholderShortValidator
import com.github.cao.awa.kora.server.network.http.placeholder.url.type.validator.TypedHttpUrlPlaceholderStringValidator
import com.github.cao.awa.kora.server.network.http.placeholder.url.type.validator.TypedHttpUrlPlaceholderValidator
import com.github.cao.awa.kora.server.network.http.context.KoraHttpContext
import com.github.cao.awa.kora.server.network.http.placeholder.url.exception.TypedHttpUrlMissingException
import kotlin.reflect.KClass

class TypedHttpUrlPlaceholder<T : Any>(val name: String, private val type: KClass<T>) {
    companion object {
        private val validators: MutableMap<KClass<*>, TypedHttpUrlPlaceholderValidator<*>> = mutableMapOf()

        private fun <T : Any> addValidator(type: KClass<T>, validator: TypedHttpUrlPlaceholderValidator<T>) {
            this.validators[type] = validator
        }

        @Suppress("unchecked_cast")
        fun <T : Any> getValidator(type: KClass<T>): TypedHttpUrlPlaceholderValidator<T>? {
            return this.validators[type] as TypedHttpUrlPlaceholderValidator<T>?
        }

        init {
            addValidator(Short::class, TypedHttpUrlPlaceholderShortValidator())
            addValidator(Int::class, TypedHttpUrlPlaceholderIntValidator())
            addValidator(Long::class, TypedHttpUrlPlaceholderLongValidator())
            addValidator(Float::class, TypedHttpUrlPlaceholderFloatValidator())
            addValidator(Double::class, TypedHttpUrlPlaceholderDoubleValidator())

            addValidator(Boolean::class, TypedHttpUrlPlaceholderBooleanValidator())

            addValidator(Byte::class, TypedHttpUrlPlaceholderByteValidator())
            addValidator(Char::class, TypedHttpUrlPlaceholderCharValidator())

            addValidator(String::class, TypedHttpUrlPlaceholderStringValidator())
        }
    }

    @Suppress("unchecked_cast")
    fun get(context: KoraHttpContext): T {
        val contentList = context.path().split("/")
        val seq: Int = context.placeholders()[this.name] ?: missing(context)

        if (contentList.size <= seq) {
            missing(context)
        }

        val validator: TypedHttpUrlPlaceholderValidator<*> = getValidator(this.type)
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

inline fun <reified T : Any> placeholder(name: String): TypedHttpUrlPlaceholder<T> {
    return TypedHttpUrlPlaceholder(name, T::class)
}