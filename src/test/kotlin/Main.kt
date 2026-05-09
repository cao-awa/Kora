import com.github.cao.awa.cason.annotation.Field
import com.github.cao.awa.kora.server.network.http.KoraHttpServer
import com.github.cao.awa.kora.server.network.http.argument.type.arg
import com.github.cao.awa.kora.server.network.http.builder.http
import com.github.cao.awa.kora.server.network.http.context.KoraHttpContext
import com.github.cao.awa.kora.server.network.http.context.abort.KoraAbortHttpContext
import com.github.cao.awa.kora.server.network.control.abort.reason.AbortReason
import com.github.cao.awa.kora.server.network.exception.abort.EndingEarlyException
import org.github.cao.awa.com.github.cao.awa.capertml.html
import org.github.cao.awa.com.github.cao.awa.capertml.style.width.DEVICE_WIDTH

fun main() {
    KoraHttpServer.instructHttpStatusCode = false

    // Define a required URL argument, get value in http scope.
    val actionArg = arg<Int>("action", false)

    val http = http {
        route("/test") {
            get {
                // Get URL input argument.
                val action = actionArg(this)

                // Render HTML page.
                html {
                    head {
                        charset(Charsets.UTF_8)
                        viewport {
                            width(DEVICE_WIDTH)
                            initialScale(1.0)
                        }
                        pageTitle {
                            +"TestPage"
                        }
                    }
                    body {
                        a {
                            href("https://www.google.com")
                            text("Google")
                        }
                        p {
                            text("Successfully input arg '${actionArg.name}', value is '$action'")
                        }
                    }
                }
            }
        }
    }

    KoraHttpServer(http).start(
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