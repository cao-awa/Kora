package com.github.cao.awa.kalmia.server.network.http.argument.type.validator.basic.string

import com.github.cao.awa.kalmia.server.network.http.argument.type.validator.TypedHttpArgumentInitializeValidator

class TypedHttpArgumentStringInitializeValidator : TypedHttpArgumentInitializeValidator<String> {
    override operator fun get(argumentName:String, content: String): String = content
}