package com.github.cao.awa.kora.server.network.http.error

import com.github.cao.awa.kora.server.network.http.argument.exception.TypedHttpArgumentMissingException
import com.github.cao.awa.kora.server.network.http.argument.type.validator.exception.TypedHttpArgumentValidateException
import io.netty.handler.codec.http.FullHttpResponse
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpVersion
import kotlin.reflect.KClass

object KoraHttpErrors {
    private val ERRORS: MutableMap<KClass<out Throwable>,  (HttpVersion, Throwable, String) -> FullHttpResponse> = HashMap()

    val FAILURE_NOT_FULL: (HttpVersion, Throwable, String) -> FullHttpResponse = { httpVersion, exception, _ ->
        KoraHttpError(
            HttpResponseStatus.BAD_REQUEST,
            httpVersion,
            exception,
            "Request is not full"
        ).createResponse()
    }

    val BAD_REQUEST: (HttpVersion, Throwable, String) -> FullHttpResponse = { httpVersion, exception, message ->
        KoraHttpError(
            HttpResponseStatus.BAD_REQUEST,
            httpVersion,
            exception,
            message
        ).createResponse()
    }

    val INTERNAL_SERVER_ERROR: (HttpVersion, Throwable, String) -> FullHttpResponse = { httpVersion, exception, message ->
        KoraHttpError(
            HttpResponseStatus.INTERNAL_SERVER_ERROR,
            httpVersion,
            exception,
            message
        ).createResponse()
    }

    fun adapter(httpVersion: HttpVersion, error: Throwable): FullHttpResponse {
        val errorProducer = ERRORS[error::class]

        if (errorProducer != null) {
            return errorProducer(httpVersion, error, error.message ?: "Unknown error")
        }
        return INTERNAL_SERVER_ERROR(httpVersion, error, "Unknown error")
    }

    init {
        ERRORS[TypedHttpArgumentMissingException::class] = BAD_REQUEST
        ERRORS[TypedHttpArgumentValidateException::class] = BAD_REQUEST
    }
}