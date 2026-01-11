import com.github.cao.awa.kora.server.network.http.KoraHttpServer
import com.github.cao.awa.kora.server.network.http.builder.server
import com.github.cao.awa.kora.server.network.response.content.NoContentResponse
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
                status = HttpResponseStatus.INTERNAL_SERVER_ERROR

                KoraResponse(
                    type = "get",
                    timestamp = System.currentTimeMillis()
                )
            }
        }

        // Test no content response.
        route("/fail") {
            post {
                NoContentResponse
            }

            get {
                NoContentResponse
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
