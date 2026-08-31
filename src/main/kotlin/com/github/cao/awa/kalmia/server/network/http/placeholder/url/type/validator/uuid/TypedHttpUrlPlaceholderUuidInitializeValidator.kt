package com.github.cao.awa.kalmia.server.network.http.placeholder.url.type.validator.uuid

import com.github.cao.awa.kalmia.server.network.http.placeholder.url.type.validator.TypedHttpUrlPlaceholderInitializeValidator
import java.util.UUID

class TypedHttpUrlPlaceholderUuidInitializeValidator : TypedHttpUrlPlaceholderInitializeValidator<UUID> {
    override operator fun get(argumentName:String, content: String, url: String): UUID = UUID.fromString(content)
}