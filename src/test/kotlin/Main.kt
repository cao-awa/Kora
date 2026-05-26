import com.github.cao.awa.kora.KoraEntrypoint
import com.github.cao.awa.kora.server.network.http.KoraHttpServer
import com.github.cao.awa.kora.server.network.http.argument.type.arg
import com.github.cao.awa.kora.server.network.http.placeholder.url.type.placeholder
import com.github.cao.awa.kora.server.network.http.builder.http
import com.github.cao.awa.kora.server.network.http.exception.path.HttpPathNotRegisteredException
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpVersion
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.github.cao.awa.com.github.cao.awa.capertml.html
import org.github.cao.awa.com.github.cao.awa.capertml.style.width.DEVICE_WIDTH
import java.nio.charset.StandardCharsets

private val LOGGER: Logger = LogManager.getLogger("Test")

// oha -n 10000 -c 128 --latency-correction http://127.0.0.1:12345/test
fun main() {
//    KoraEntrypoint.main()
    testValidator()
}

fun testValidator() {
    val username = arg<String>("username").validator { content ->
        if (content.length < 5) {
            throw IllegalArgumentException("Username length must more than 5 characters")
        }
        content
    }

    val api = http {
        route("/register") {
            get {
                val name = username(this)
                println("User $name registered")
                html {
                    body {
                        p {
                            +"User $name registered now!"
                        }
                    }
                }
            }
        }
    }

    KoraHttpServer(api).start(
        port = 12345
    )
}

fun testBuild() {
    val username = arg<String>("username", false)
    val password = arg<String>("password", false)

    data class LoginRequest(val username: String, val password: String)

    val api = http {
        route("test") {
            get {
                val loginRequest = build(
                    username,
                    password,
                    // Use lambda constructor.
                    ::LoginRequest
                )
                // Call auth plugin codes here...

                // Or:
                // val loginRequest2 = build(
                //    username,
                //    password
                // ) { name, pwd ->
                //   // Construct manually.
                //    LoginRequest(name, pwd)
                // }
            }
        }
    }

    KoraHttpServer(api).start(
        port = 12345
    )
}

fun testSimple() {
    val html = html {
        head {
            charset(StandardCharsets.UTF_8)
        }
        body {
            p {
                +"Hello world!"
            }
        }
    }

    val http = http {
        route("/test") {
            get {
                html
            }
        }
    }

    KoraHttpServer(http).start(
        port = 12345
    )
}

fun testAssets() {
    val http = http {
        // Setup static asset path.
        assets("assets/")

        // Redirect all no registered query to 404 page.
        ifAbort(HttpPathNotRegisteredException::class) {
            withAsset(redirectAsset = "error/404.html")
        }
    }

    KoraHttpServer(http).start(
        port = 12345
    )
}

fun testDataClass() {
    data class Data(val name: String, val age: Int)

    val http = http {
        route("/test") {
            get {
                Data(
                    name = "cao-awa",
                    age = 17
                )
            }
        }
    }

    KoraHttpServer(http).start(
        port = 12345
    )
}

fun testPlaceholder() {
    val http = http {
        assets("assets")
        val userId = placeholder<Int>(name = "userId")
        val testId = placeholder<Int>(name = "testId")

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

        route("/test/qaq/awa") {
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

        ifAbort(HttpPathNotRegisteredException::class) { exception ->
            LOGGER.error(exception)

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
        port = 12345
    )
}

fun testNotFound() {
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

        ifAbort(HttpPathNotRegisteredException::class) { exception ->
            LOGGER.error(exception)

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
        port = 12345
    )
}

fun testError() {
    val http = http {
        route("/test") {
            get {
                // Simulation code wrongs.
                abortWith(NullPointerException("Test if logic error occurs NPE"), HttpResponseStatus.BAD_REQUEST, this)
            }.ifAbort(NullPointerException::class) { exception ->
                LOGGER.error(exception)
                val httpProtocolVersion: HttpVersion = protocolVersion()
                LOGGER.error("Http protocol version: $httpProtocolVersion")
                val status: HttpResponseStatus = status()
                LOGGER.error("Http status: $status")
            }
        }
    }

    KoraHttpServer(http).start(
        port = 12345
    )
}

fun testRender() {
    // Define a required URL argument, get value in http scope.
    val actionArg = arg<Int>(name = "action", missable = false)

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
        port = 12345
    )
}