package com.github.cao.awa.kalmia.server.network.http.argument.type.validator.uuid

import com.github.cao.awa.kalmia.server.network.http.argument.type.validator.TypedHttpArgumentInitializeValidator
import java.util.UUID

class TypedHttpArgumentUuidInitializeValidator : TypedHttpArgumentInitializeValidator<UUID> {
    override operator fun get(argumentName: String, content: String): UUID = UUID.fromString(content)
}