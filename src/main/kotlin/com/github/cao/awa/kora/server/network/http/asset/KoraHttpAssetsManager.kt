package com.github.cao.awa.kora.server.network.http.asset

import com.github.cao.awa.kora.server.network.http.file.header.KoraHttpFileExtentions
import com.github.cao.awa.kora.server.network.http.context.KoraHttpContext
import java.io.File

class KoraHttpAssetsManager {
    private var path: String = ""

    fun setAssetsPath(path: String) {
        if (this.path != "") {
            throw IllegalArgumentException("Assets path already set")
        } else {
            this.path = path
        }
    }

    fun getAsset(name: String): File? {
        val asset = File("${this.path}/${ name}")
        if (asset.isFile && asset.exists()) {
            return asset
        }
        return null
    }

    fun response(context: KoraHttpContext, response: File): ByteArray {
        val data = response.readBytes()
        context.withContentType(
            KoraHttpFileExtentions.getContentType(
                response
            )
        )
        return data
    }
}