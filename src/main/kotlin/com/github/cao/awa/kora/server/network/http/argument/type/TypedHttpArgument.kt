package com.github.cao.awa.kora.server.network.http.argument.type

import com.github.cao.awa.kora.server.network.http.argument.exception.TypedHttpArgumentMissingException
import com.github.cao.awa.kora.server.network.http.argument.type.validator.basic.TypedHttpArgumentBooleanInitializeValidator
import com.github.cao.awa.kora.server.network.http.argument.type.validator.basic.TypedHttpArgumentByteInitializeValidator
import com.github.cao.awa.kora.server.network.http.argument.type.validator.basic.TypedHttpArgumentCharInitializeValidator
import com.github.cao.awa.kora.server.network.http.argument.type.validator.data.TypedHttpArgumentDataInitializeValidator
import com.github.cao.awa.kora.server.network.http.argument.type.validator.basic.TypedHttpArgumentDoubleInitializeValidator
import com.github.cao.awa.kora.server.network.http.argument.type.validator.basic.TypedHttpArgumentFloatInitializeValidator
import com.github.cao.awa.kora.server.network.http.argument.type.validator.basic.TypedHttpArgumentIntInitializeValidator
import com.github.cao.awa.kora.server.network.http.argument.type.validator.basic.TypedHttpArgumentLongInitializeValidator
import com.github.cao.awa.kora.server.network.http.argument.type.validator.basic.TypedHttpArgumentShortInitializeValidator
import com.github.cao.awa.kora.server.network.http.argument.type.validator.basic.string.TypedHttpArgumentStringInitializeValidator
import com.github.cao.awa.kora.server.network.http.argument.type.validator.TypedHttpArgumentInitializeValidator
import com.github.cao.awa.kora.server.network.http.argument.type.validator.TypedHttpArgumentValidator
import com.github.cao.awa.kora.server.network.http.argument.type.validator.exception.TypedHttpArgumentValidateException
import com.github.cao.awa.kora.server.network.http.argument.type.value.TypedHttpArgumentDefaultValues
import com.github.cao.awa.kora.server.network.http.context.KoraHttpContext
import kotlin.reflect.KClass

class TypedHttpArgument<T : Any> {
    companion object {
        private val validators: MutableMap<KClass<*>, TypedHttpArgumentInitializeValidator<*>> = mutableMapOf()

        init {
            addValidator(Short::class, TypedHttpArgumentShortInitializeValidator())
            addValidator(Int::class, TypedHttpArgumentIntInitializeValidator())
            addValidator(Long::class, TypedHttpArgumentLongInitializeValidator())
            addValidator(Float::class, TypedHttpArgumentFloatInitializeValidator())
            addValidator(Double::class, TypedHttpArgumentDoubleInitializeValidator())

            addValidator(Boolean::class, TypedHttpArgumentBooleanInitializeValidator())

            addValidator(Byte::class, TypedHttpArgumentByteInitializeValidator())
            addValidator(Char::class, TypedHttpArgumentCharInitializeValidator())

            addValidator(String::class, TypedHttpArgumentStringInitializeValidator())
        }

        fun <T : Any> addValidator(type: KClass<T>, validator: TypedHttpArgumentInitializeValidator<T>) {
            this.validators[type] = validator
        }

        fun <T : Any> addValidator(type: KClass<T>, validator: (String, String) -> T) {
            this.validators[type] = TypedHttpArgumentDataInitializeValidator(validator)
        }

        @Suppress("unchecked_cast")
        fun <T : Any> getValidator(type: KClass<T>): TypedHttpArgumentInitializeValidator<T>? {
            return this.validators[type] as TypedHttpArgumentInitializeValidator<T>?
        }

        inline fun <reified T : Any> getActualValidator(type: KClass<T>): TypedHttpArgumentInitializeValidator<T> {
            return getValidator(type)
                ?: TypedHttpArgumentValidateException.failed("Unregistered argument validator of type '${type}'")
        }

        inline fun <reified T : Any> create(name: String, type: KClass<T>, missable: Boolean): TypedHttpArgument<T> {
            return TypedHttpArgument(name, type, missable, getActualValidator(T::class))
        }
    }

    val name: String
    val missable: Boolean
    private val type: KClass<T>
    private var defaultValue: T? = null
    private val initializeValidator: TypedHttpArgumentInitializeValidator<T>
    private var customValidator: MutableList<TypedHttpArgumentValidator<T>> = mutableListOf()

    constructor(
        name: String,
        type: KClass<T>,
        missable: Boolean,
        initializeValidator: TypedHttpArgumentInitializeValidator<T>
    ) {
        this.name = name
        this.type = type
        this.missable = missable
        this.initializeValidator = initializeValidator
    }

    @Suppress("unchecked_cast")
    operator fun get(context: KoraHttpContext): T {
        val content: String = context.arguments()[this.name]
            ?: TypedHttpArgumentMissingException.missing("Required argument '${this.name}' is missing, type is ${this.type.simpleName}")
        var value: T = this.initializeValidator[this.name, content]
        for (validator in customValidator) {
            value = validator.validate(value)
        }
        return value
    }

    operator fun invoke(context: KoraHttpContext): T {
        return try {
            get(context)
        } catch (e: Exception) {
            if (this.missable) {
                if (this.defaultValue == null) {
                    TypedHttpArgumentDefaultValues.getDefault(this.type)
                } else {
                    this.defaultValue!!
                }
            } else {
                throw e
            }
        }
    }

    fun defaultValue(value: T): TypedHttpArgument<T> {
        this.defaultValue = value
        return this
    }

    fun validator(validator: TypedHttpArgumentValidator<T>): TypedHttpArgument<T> {
        this.customValidator.add(validator)
        return this
    }

    fun validator(validator: (T) -> T): TypedHttpArgument<T> {
        validator(
            object : TypedHttpArgumentValidator<T> {
                override fun validate(value: T): T {
                    return validator(value)
                }
            }
        )
        return this
    }
}

inline fun <reified T : Any> arg(name: String, missable: Boolean = false): TypedHttpArgument<T> {
    return TypedHttpArgument.create(name, T::class, missable)
}
