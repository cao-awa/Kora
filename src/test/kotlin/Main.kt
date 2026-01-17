import com.github.cao.awa.cason.annotation.Field
import com.github.cao.awa.kora.server.network.http.KoraHttpServer
import com.github.cao.awa.kora.server.network.http.argument.type.arg
import com.github.cao.awa.kora.server.network.http.builder.http
import com.github.cao.awa.kora.server.network.http.context.KoraHttpContext
import com.github.cao.awa.kora.server.network.http.context.abort.KoraAbortHttpContext
import com.github.cao.awa.kora.server.network.http.control.abort.reason.AbortReason
import com.github.cao.awa.kora.server.network.http.exception.abort.EndingEarlyException

fun main() {
    KoraHttpServer.instructHttpStatusCode = false

    val intArg = arg<Int>("action")

    val api = http {
        route("/test") {
            get {
                val action = intArg(this)

                println(action)

                testGet()
            }

            post {
                testPost()
            }.abort { reason ->
                testHandleAbort(reason)
            }.abort(NullPointerException::class) { reason ->
                testHandleNPE(reason)
            }
        }
    }

    KoraHttpServer(api).start(
        port = 12345,
        useEpoll = true
    )
}

fun KoraHttpContext.testGet(): KoraResponse {
    return KoraResponse(
        type = "get: ${params()}, ${arguments()}",
        timestamp = System.currentTimeMillis()
    )
}

fun KoraHttpContext.testPost(): KoraResponse {
    return KoraResponse(
        type = "post: ${params()}, ${arguments()}",
        timestamp = System.currentTimeMillis()
    )
}

fun KoraAbortHttpContext.testHandleAbort(reason: AbortReason<EndingEarlyException>): KoraErrorResponse {
    // Use logging in the future.
    println("Abort with: ${reason.reason}")
    reason.exception.printStackTrace()
    return KoraErrorResponse(
        "Error: controlled abort",
        status().code(),
        System.currentTimeMillis()
    )
}

fun KoraAbortHttpContext.testHandleNPE(reason: AbortReason<NullPointerException>): KoraErrorResponse {
    // Use logging in the future.
    println("Abort with: ${reason.reason}")
    reason.exception.printStackTrace()
    return KoraErrorResponse(
        "Error: ${reason.reason}",
        status().code(),
        System.currentTimeMillis()
    )
}

data class KoraResponse(
    val type: String,
    val timestamp: Long
)

data class KoraErrorResponse(
    @Field("error_details")
    val errorDetails: String,
    @Field("error_code")
    val code: Int,
    val timestamp: Long
)