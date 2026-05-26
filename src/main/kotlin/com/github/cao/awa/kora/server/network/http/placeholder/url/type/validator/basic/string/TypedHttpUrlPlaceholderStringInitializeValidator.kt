package com.github.cao.awa.kora.server.network.http.placeholder.url.type.validator.basic.string

import com.github.cao.awa.kora.server.network.http.placeholder.url.type.validator.TypedHttpUrlPlaceholderInitializeValidator

class TypedHttpUrlPlaceholderStringInitializeValidator : TypedHttpUrlPlaceholderInitializeValidator<String> {
    override operator fun get(argumentName:String, content: String, url: String): String = content
}