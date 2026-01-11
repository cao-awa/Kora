import com.github.cao.awa.kora.server.network.http.KoraHttpServer
import com.github.cao.awa.kora.server.network.http.builder.server
import io.netty.handler.codec.http.HttpResponseStatus

fun main() {
    KoraHttpServer.instructHttpStatusCode = false

    val api = server {
        route("/test") {
            post {
                KoraResponse(
                    type = "post",
                    timestamp = System.currentTimeMillis()
                )
            }

            get {
                status = HttpResponseStatus.INTERNAL_SERVER_ERROR

                KoraResponse(
                    type = "get",
                    timestamp = System.currentTimeMillis()
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
