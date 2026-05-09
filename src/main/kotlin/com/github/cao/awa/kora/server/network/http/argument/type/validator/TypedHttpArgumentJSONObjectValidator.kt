package com.github.cao.awa.kora.server.network.http.argument.type.validator

import com.github.cao.awa.cason.obj.JSONObject
import com.github.cao.awa.cason.serialize.parser.JSONParser

interface TypedHttpArgumentJSONObjectValidator<T : Any> : TypedHttpArgumentValidator<T> {
    override operator fun get(argumentName: String, content: String): T {
        return get(argumentName, JSONParser.parseObject(content))
    }

    operator fun get(argumentName: String, content: JSONObject): T
}