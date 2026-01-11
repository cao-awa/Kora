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
                abortWith(HttpResponseStatus.INTERNAL_SERVER_ERROR) {

                }
                KoraResponse(
                    type = "get",
                    timestamp = System.currentTimeMillis()
                )
            }.abort {
                KoraErrorResponse(
                    "Error awa",
                    500,
                    System.currentTimeMillis()
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

data class KoraErrorResponse(
    val error: String,
    val code: Int,
    val timestamp: Long
)