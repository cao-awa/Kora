package com.github.cao.awa.kalmia.server.network.http.placeholder.url.type.validator.basic

import com.github.cao.awa.kalmia.server.network.http.placeholder.url.type.validator.TypedHttpUrlPlaceholderInitializeValidator
import com.github.cao.awa.kalmia.server.network.http.placeholder.url.type.validator.error

class TypedHttpUrlPlaceholderLongInitializeValidator : TypedHttpUrlPlaceholderInitializeValidator<Long> {
    override operator fun get(argumentName:String, content: String, url: String): Long {
        try {
            return java.lang.Long.parseLong(content)
        } catch (_: NumberFormatException) {
            error(argumentName, content, "Long", url)
        }
    }
}