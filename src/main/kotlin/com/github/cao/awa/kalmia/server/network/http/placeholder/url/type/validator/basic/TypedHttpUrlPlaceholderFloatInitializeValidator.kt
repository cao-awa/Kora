package com.github.cao.awa.kalmia.server.network.http.placeholder.url.type.validator.basic

import com.github.cao.awa.kalmia.server.network.http.placeholder.url.type.validator.TypedHttpUrlPlaceholderInitializeValidator
import com.github.cao.awa.kalmia.server.network.http.placeholder.url.type.validator.error

class TypedHttpUrlPlaceholderFloatInitializeValidator : TypedHttpUrlPlaceholderInitializeValidator<Float> {
    override operator fun get(argumentName:String, content: String, url: String): Float {
        try {
            return java.lang.Float.parseFloat(content)
        } catch (_: NumberFormatException) {
            error(argumentName, content, "Float", url)
        }
    }
}