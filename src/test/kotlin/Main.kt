import com.github.cao.awa.kora.server.network.http.KoraHttpServer

fun main() {
    KoraHttpServer{

    }.start(
        12345,
        true
    )
}