import com.github.cao.awa.kora.server.network.http.KoraHttpServer
import com.github.cao.awa.kora.server.network.http.argument.type.arg
import com.github.cao.awa.kora.server.network.http.placeholder.url.type.placeholder
import com.github.cao.awa.kora.server.network.http.builder.http
import com.github.cao.awa.kora.server.network.http.path.exception.HttpPathNotRegisteredException
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpVersion
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.github.cao.awa.com.github.cao.awa.capertml.html
import org.github.cao.awa.com.github.cao.awa.capertml.style.width.DEVICE_WIDTH

private val LOGGER: Logger = LogManager.getLogger("Test")

fun main() {
    testPlaceholder()
}

fun testPlaceholder(){
    KoraHttpServer.instructHttpStatusCode = false

    val http = http {
        val userId = placeholder<Int>("userId")
        val testId = placeholder<Int>("testId")

        route("/test/{userId}/{testId}") {
            get {
                val userIdValue = userId(this)
                val testIdValue = testId(this)

                // Render HTML page.
                html {
                    head {
                        charset(Charsets.UTF_8)
                    }
                    body {
                        p {
                            text("Page '/${path()}' has loaded! with userId '$userIdValue' and testId '$testIdValue'")
                        }
                    }
                }
            }
        }

        ifAbort(HttpPathNotRegisteredException::class) { context ->
            LOGGER.error(context.exception)

            // Render HTML page.
            html {
                head {
                    charset(Charsets.UTF_8)
                }
                body {
                    p {
                        text("Page '/${path()}' not found!")
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

fun testNotFound(){
    KoraHttpServer.instructHttpStatusCode = false

    val http = http {
        route("/test/qaq") {
            get {
                // Render HTML page.
                html {
                    head {
                        charset(Charsets.UTF_8)
                    }
                    body {
                        p {
                            text("Page '/${path()}' has loaded!")
                        }
                    }
                }
            }
        }

        ifAbort(HttpPathNotRegisteredException::class) { context ->
            LOGGER.error(context.exception)

            // Render HTML page.
            html {
                head {
                    charset(Charsets.UTF_8)
                }
                body {
                    p {
                        text("Page '/${path()}' not found!")
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

fun testError() {
    KoraHttpServer.instructHttpStatusCode = false

    val http = http {
        route("/test") {
            get {
                // Simulation code wrongs.
                abortWith(NullPointerException("Test if logic error occurs NPE"), HttpResponseStatus.BAD_REQUEST, this)
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