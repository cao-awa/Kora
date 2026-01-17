import com.github.cao.awa.cason.annotation.Field
import com.github.cao.awa.kora.server.network.http.KoraHttpServer
import com.github.cao.awa.kora.server.network.http.builder.http
import com.github.cao.awa.kora.server.network.http.context.KoraHttpContext
import com.github.cao.awa.kora.server.network.http.context.abort.KoraAbortHttpContext
import com.github.cao.awa.kora.server.network.http.control.abort.reason.AbortReason
import com.github.cao.awa.kora.server.network.http.exception.abort.EndingEarlyException

fun main() {
    KoraHttpServer.instructHttpStatusCode = false

    val api = http {
        route("/test") {
            get {
                testGet(this)
            }

            post {
                testPost(this)
            }.abort { reason ->
                testHandleAbort(this, reason)
            }.abort(NullPointerException::class) { reason ->
                testHandleNPE(this, reason)
            }
        }
    }

    KoraHttpServer(api).start(
        port = 12345,
        useEpoll = true
    )
}

fun testGet(context: KoraHttpContext): KoraResponse {
    return KoraResponse(
        type = "get: ${context.params()}, ${context.arguments()}",
        timestamp = System.currentTimeMillis()
    )
}

fun testPost(context: KoraHttpContext): KoraResponse {
    return KoraResponse(
        type = "post: ${context.params()}, ${context.arguments()}",
        timestamp = System.currentTimeMillis()
    )
}

fun testHandleAbort(context: KoraAbortHttpContext, reason: AbortReason<EndingEarlyException>): KoraErrorResponse {
    // Use logging in the future.
    println("Abort with: ${reason.reason}")
    reason.exception.printStackTrace()
    return KoraErrorResponse(
        "Error: controlled abort",
        context.status().code(),
        System.currentTimeMillis()
    )
}

fun testHandleNPE(context: KoraAbortHttpContext, reason: AbortReason<NullPointerException>): KoraErrorResponse {
    // Use logging in the future.
    println("Abort with: ${reason.reason}")
    reason.exception.printStackTrace()
    return KoraErrorResponse(
        "Error: ${reason.reason}",
        context.status().code(),
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