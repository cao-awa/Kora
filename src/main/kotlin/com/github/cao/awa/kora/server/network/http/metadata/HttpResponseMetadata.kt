package com.github.cao.awa.kora.server.network.http.metadata

import com.github.cao.awa.cason.annotation.Field

data class HttpResponseMetadata(
    @Field("http_status")
    val status: Int?,
    @Field("http_version")
    val protocolVersion: String?
)