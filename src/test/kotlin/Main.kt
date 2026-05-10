import com.github.cao.awa.kora.server.network.http.KoraHttpServer
import com.github.cao.awa.kora.server.network.http.argument.type.arg
import com.github.cao.awa.kora.server.network.http.builder.http
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpVersion
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.github.cao.awa.com.github.cao.awa.capertml.html
import org.github.cao.awa.com.github.cao.awa.capertml.style.width.DEVICE_WIDTH

private val LOGGER: Logger = LogManager.getLogger("Test")

fun main() {
    testRender()
}

fun testError() {
    KoraHttpServer.instructHttpStatusCode = false

    val http = http {
        route("/test") {
            get {
                // Simulation code wrongs.
                throw NullPointerException("Test if logic error occurs NPE")
            }.ifAbort(NullPointerException::class) { context ->
                LOGGER.error(context.exception)
                val httpProtocolVersion: HttpVersion = protocolVersion()
                LOGGER.error("Http protocol version: $httpProtocolVersion")
                val status: HttpResponseStatus = status()
                LOGGER.error("Http status: $status")
            }
        }
    }

    KoraHttpServer(http).start(
        port = 12345,
        useEpoll = true
    )
}

fun testRender() {
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
                        refresh {
                            jumpUrl("/awa")
                            waitTime(0)
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