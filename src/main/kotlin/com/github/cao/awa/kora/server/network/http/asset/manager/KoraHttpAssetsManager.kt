package com.github.cao.awa.kora.server.network.http.asset.manager

import com.github.cao.awa.kora.constant.KoraInformation
import com.github.cao.awa.kora.server.network.http.asset.KoraAsset
import com.github.cao.awa.kora.server.network.http.asset.KoraBinaryAsset
import com.github.cao.awa.kora.server.network.http.content.type.HttpContentTypes
import com.github.cao.awa.kora.server.network.http.context.KoraHttpContext
import com.github.cao.awa.kora.server.network.http.file.header.KoraHttpFileExtentions
import com.github.cao.awa.kora.server.network.http.exception.path.HttpPathNotRegisteredException
import com.github.cao.awa.kora.server.network.http.php.KoraHttpPHPNotFoundException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets

class KoraHttpAssetsManager {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger("KoraHttpAssetsManager")
    }

    private var path: String = ""
    private var available: Boolean = false

    fun setAssetsPath(path: String) {
        if (this.available) {
            throw IllegalArgumentException("Assets path already set")
        } else {
            this.path = File(path).absolutePath
            this.available = true
        }
    }

    fun available(): Boolean = this.available

    fun hasAsset(context: KoraHttpContext): Boolean {
        return createFileHolder(context.path()).exists()
    }

    fun getAsset(context: KoraHttpContext): KoraBinaryAsset {
        val assetName = if (context.redirectAsset() == "") {
            context.path()
        } else {
            context.redirectAsset()
        }
        val assetFile = createFileHolder(assetName)
        if (assetFile.isFile && assetFile.exists()) {
            return KoraBinaryAsset(assetFile)
        }
        HttpPathNotRegisteredException.notFound(assetName, "auto redirect")
    }

    fun createFileHolder(name: String): File {
        return File("${this.path}/${name}")
    }

    fun handlePhp(context: KoraHttpContext, asset: KoraAsset<*>): String {
        try {
            val rootFile = File(this.path)
            val process = ProcessBuilder(
                "php-cgi",
                "-d", "default_charset=\"UTF-8\"",
                "-d", "mbstring.http_output=\"UTF-8\"",
                rootFile.absolutePath
            ).also { builder ->
                builder.environment()["REQUEST_METHOD"] = context.method().name()
                builder.environment()["SCRIPT_FILENAME"] = asset.file.absolutePath
                builder.environment()["CONTENT_LENGTH"] = context.contentLength().toString()
                builder.environment()["SERVER_PROTOCOL"] = context.protocolVersion().toString()
                builder.environment()["CONTENT_TYPE"] = context.contentType().toString()
                builder.environment()["SERVER_SOFTWARE"] = KoraInformation.SOFTWARE_NAME
                builder.environment()["REQUEST_URI"] = context.path()
                builder.environment()["REDIRECT_STATUS"] = "200"
                builder.environment()["DOCUMENT_ROOT"] = rootFile.absolutePath
            }.start()

            process.outputStream.use {
                it.write(context.content())
            }

            val response = process.inputStream.readBytes()
            process.waitFor()

            val phpResponse = String(response, StandardCharsets.UTF_8)

            val headers: MutableMap<String, Any> = HashMap()

            if (phpResponse.contains("<!DOCTYPE html>")) {
                val headerContent = phpResponse.substring(0, phpResponse.indexOf("<!DOCTYPE html>") - 1)
                val responseContent = phpResponse.substring(phpResponse.indexOf("<!DOCTYPE html>"))

                val headersContent = headerContent.split("\n")

                val status = headersContent[0]

                var index = 1
                while (index < headersContent.size) {
                    val header = headersContent[index]
                    if (header.indexOf(":") == -1) {
                        break
                    }
                    headers[header.substring(0, header.indexOf(":"))] = header.substring(header.indexOf(":") + 1)
                    index++
                }

                return responseContent
            }
            return phpResponse
        } catch (e: IOException) {
            KoraHttpPHPNotFoundException.notFoundPHP("Please ensure PHP installed in your server")
        }
    }

    fun createResponse(context: KoraHttpContext, asset: KoraAsset<*>?): Any {
        if (asset != null) {
            var data = asset.data
            val contentType = KoraHttpFileExtentions.getContentType(
                asset.file
            )
            if (contentType == HttpContentTypes.PHP) {
                data = handlePhp(context, asset)
            }
            context.withContentType(
                contentType
            )
            return data
        }
        HttpPathNotRegisteredException.notFound(context.path())
    }

    fun createResponse(context: KoraHttpContext): Any {
        return createResponse(context, getAsset(context))
    }
}