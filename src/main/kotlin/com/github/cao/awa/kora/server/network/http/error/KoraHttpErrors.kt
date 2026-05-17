package com.github.cao.awa.kora.server.network.http.error

import com.github.cao.awa.kora.server.network.http.argument.exception.TypedHttpArgumentMissingException
import com.github.cao.awa.kora.server.network.http.argument.type.validator.exception.TypedHttpArgumentValidateException
import com.github.cao.awa.kora.server.network.http.context.KoraHttpContext
import com.github.cao.awa.kora.server.network.http.exception.path.HttpPathNotRegisteredException
import io.netty.handler.codec.http.FullHttpResponse
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpVersion
import kotlin.reflect.KClass

object KoraHttpErrors {
    private val ERRORS: MutableMap<KClass<out Throwable>,  (HttpVersion, Throwable, String, String, KoraHttpContext?) -> FullHttpResponse> = HashMap()

    val FAILURE_NOT_FULL: (HttpVersion, Throwable, String, String,  KoraHttpContext?) -> FullHttpResponse = { httpVersion, exception, _, requestPath, context ->
        KoraHttpError(
            HttpResponseStatus.BAD_REQUEST,
            httpVersion,
            exception,
            "Request is not full",
            requestPath,
            context
        ).createResponse()
    }

    val NOT_FOUND: (HttpVersion, Throwable, String, String,   KoraHttpContext?) -> FullHttpResponse = { httpVersion, exception, _, requestPath,  context ->
        KoraHttpError(
            HttpResponseStatus.NOT_FOUND,
            httpVersion,
            exception,
            "Page not found",
            requestPath,
            context
        ).createResponse()
    }


    val BAD_REQUEST: (HttpVersion, Throwable, String, String,  KoraHttpContext?) -> FullHttpResponse = { httpVersion, exception, message, requestPath,  context ->
        KoraHttpError(
            HttpResponseStatus.BAD_REQUEST,
            httpVersion,
            exception,
            message,
            requestPath,
            context
        ).createResponse()
    }

    val INTERNAL_SERVER_ERROR: (HttpVersion, Throwable, String, String,   KoraHttpContext?) -> FullHttpResponse = { httpVersion, exception, message, requestPath,  context ->
        KoraHttpError(
            HttpResponseStatus.INTERNAL_SERVER_ERROR,
            httpVersion,
            exception,
            message,
            requestPath,
            context
        ).createResponse()
    }

    fun adapter(httpVersion: HttpVersion, requestPath: String, error: Throwable): FullHttpResponse {
        val errorProducer = ERRORS[error::class]

        if (errorProducer != null) {
            return errorProducer(httpVersion, error, error.message ?: "Unknown error", requestPath, null)
        }
        return INTERNAL_SERVER_ERROR(httpVersion, error, error.message?: "Unknown error", requestPath, null)
    }

    fun adapter(httpVersion: HttpVersion, error: Throwable, koraContext: KoraHttpContext): FullHttpResponse {
        val errorProducer = ERRORS[error::class]

        if (errorProducer != null) {
            return errorProducer(httpVersion, error, error.message ?: "Unknown error", koraContext.path(), koraContext)
        }
        return INTERNAL_SERVER_ERROR(httpVersion, error, error.message?: "Unknown error", koraContext.path(), koraContext)
    }

    init {
        ERRORS[TypedHttpArgumentMissingException::class] = BAD_REQUEST
        ERRORS[TypedHttpArgumentValidateException::class] = BAD_REQUEST
        ERRORS[HttpPathNotRegisteredException::class] = NOT_FOUND
    }
}