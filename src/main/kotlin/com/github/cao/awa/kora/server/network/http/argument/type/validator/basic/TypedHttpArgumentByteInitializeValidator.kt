package com.github.cao.awa.kora.server.network.http.argument.type.validator.basic

import com.github.cao.awa.kora.server.network.http.argument.type.validator.TypedHttpArgumentInitializeValidator
import com.github.cao.awa.kora.server.network.http.argument.type.validator.error

class TypedHttpArgumentByteInitializeValidator : TypedHttpArgumentInitializeValidator<Byte> {
    override operator fun get(argumentName:String, content: String): Byte {
        try {
            return java.lang.Byte.parseByte(content)
        } catch (_: NumberFormatException) {
            error(argumentName, content, "Byte")
        }
    }
}