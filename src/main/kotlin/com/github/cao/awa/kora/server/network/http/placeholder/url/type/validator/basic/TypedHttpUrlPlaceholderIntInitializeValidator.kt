package com.github.cao.awa.kora.server.network.http.placeholder.url.type.validator.basic

import com.github.cao.awa.kora.server.network.http.placeholder.url.type.validator.TypedHttpUrlPlaceholderInitializeValidator
import com.github.cao.awa.kora.server.network.http.placeholder.url.type.validator.error

class TypedHttpUrlPlaceholderIntInitializeValidator : TypedHttpUrlPlaceholderInitializeValidator<Int> {
    override operator fun get(argumentName:String, content: String, url: String): Int {
        try {
            return Integer.parseInt(content)
        } catch (_: NumberFormatException) {
            error(argumentName, content, "Int", url)
        }
    }
}