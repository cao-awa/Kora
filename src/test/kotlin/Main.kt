import com.github.cao.awa.cason.annotation.Field
import com.github.cao.awa.kora.server.network.http.KoraHttpServer
import com.github.cao.awa.kora.server.network.http.builder.http
import com.github.cao.awa.kora.server.network.response.content.NoContentResponse
import io.netty.handler.codec.http.HttpResponseStatus
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.atomics.AtomicInt

fun main() {
    KoraHttpServer.instructHttpStatusCode = false

    val count = AtomicInteger(0)

    Thread {
        while (true) {
            Thread.sleep(1000)
            println("QPS: ${count.get()}")
            count.set(0)
        }
    }.start()

    val api = http {
        route("/test") {
            get {
                count.addAndGet(1)
                NoContentResponse
            }

            post {
//                if (testCondition) {
//                    abortWith(HttpResponseStatus.UNAUTHORIZED)
//                } else if (testCustomAbortCondition) {
//                    abortWith(NullPointerException("Test NPE"), HttpResponseStatus.INTERNAL_SERVER_ERROR)
//                }

                KoraResponse(
                    type = "post",
                    timestamp = System.currentTimeMillis()
                )
            }.abort { (exception, reason) ->
                // Use logging in the future.
                println("Abort with: $reason")
                exception.printStackTrace()
                KoraErrorResponse(
                    "Error: controlled abort",
                    status().code(),
                    System.currentTimeMillis()
                )
            }.abort(NullPointerException::class) { (exception, reason) ->
                // Use logging in the future.
                println("Abort with: $reason")
                exception.printStackTrace()
                KoraErrorResponse(
                    "Error: $reason",
                    status().code(),
                    System.currentTimeMillis()
                )
            }
        }
    }

    KoraHttpServer(api).start(
        port = 12345,
        useEpoll = true
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