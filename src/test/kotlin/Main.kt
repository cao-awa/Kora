import com.github.cao.awa.cason.annotation.Field
import com.github.cao.awa.kora.server.network.http.KoraHttpServer
import com.github.cao.awa.kora.server.network.http.builder.server
import io.netty.handler.codec.http.HttpResponseStatus

fun main() {
    val api = server {
        route("/test") {
            post {
                KoraResponse(
                    type = "post",
                    timestamp = System.currentTimeMillis()
                )
            }

            get {
                abortIf(!auth(), HttpResponseStatus.INTERNAL_SERVER_ERROR)

                KoraResponse(
                    type = "get",
                    timestamp = System.currentTimeMillis()
                )
            }.abort { (_, reason) ->
                println("Abort with: $reason")
                KoraNotAuthResponse(
                    "Error not authed",
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

fun auth(): Boolean {
    // Simulation auth step.
    return false
}

data class KoraResponse(
    val type: String,
    val timestamp: Long
)

data class KoraNotAuthResponse(
    @Field("error_details")
    val errorDetails: String,
    val code: Int,
    val timestamp: Long
)