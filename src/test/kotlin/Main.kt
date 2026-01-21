import com.github.cao.awa.cason.annotation.Field
import com.github.cao.awa.kora.server.network.http.KoraHttpServer
import com.github.cao.awa.kora.server.network.http.argument.type.arg
import com.github.cao.awa.kora.server.network.http.builder.http
import com.github.cao.awa.kora.server.network.http.context.KoraHttpContext
import com.github.cao.awa.kora.server.network.http.context.abort.KoraAbortHttpContext
import com.github.cao.awa.kora.server.network.control.abort.reason.AbortReason
import com.github.cao.awa.kora.server.network.exception.abort.EndingEarlyException
import com.github.cao.awa.kora.server.network.ws.KoraWebSocketServer
import com.github.cao.awa.kora.server.network.ws.builder.websocket
import java.io.File
import java.io.FileWriter
import java.io.OutputStreamWriter

fun main() {
    KoraHttpServer.instructHttpStatusCode = false

    val intArg = arg<Int>("action")

    val http = http {
        route("/test") {
            get {
                val action = intArg(this)

                println(action)

                testGet()
            }

            post {
                testPost()
            }.abort { reason ->
                testHandleAbort(reason)
            }.abort(NullPointerException::class) { reason ->
                testHandleNPE(reason)
            }
        }
    }

//    KoraHttpServer(http).start(
//        port = 12345,
//        useEpoll = true
//    )

    val files: MutableMap<String, OutputStreamWriter> = mutableMapOf()
    val compute: (String, String) -> OutputStreamWriter = { type, id ->
        val key = "C:\\Users\\cao_awa\\Documents\\NapCatQQ\\records\\qq\\chat_records\\$type\\$id.txt"
        if (!files.containsKey(key)) {
            val file = File(key)
            file.parentFile.mkdirs()
            files[key] = file.writer()
        }
        files[key]!!
    }

    val ws = websocket {
        route("qq") {
            onMessage {
                try {
                    jsonContent().also {
                        val type = it.getString("message_type")

                        when (type) {
                            "group" -> compute(type, it.getLong("group_id").toString())
                            "private" -> compute(type, it.getLong("target_id").toString())
                            else -> null
                        }.let {
                            if (it != null) {
                                it.write(stringContent())
                                it.write("\n")
                                it.flush()
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                Unit
            }
        }
    }

    KoraWebSocketServer(ws).start(
        port = 8082,
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