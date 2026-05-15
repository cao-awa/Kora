package com.github.cao.awa.kora.server.network.http.asset.manager

import com.github.cao.awa.kora.server.network.http.asset.KoraAsset
import com.github.cao.awa.kora.server.network.http.context.KoraHttpContext
import com.github.cao.awa.kora.server.network.http.file.header.KoraHttpFileExtentions
import com.github.cao.awa.kora.server.network.http.path.exception.HttpPathNotRegisteredException
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

    fun getAsset(context: KoraHttpContext): KoraAsset {
        val assetName = if (context.redirectAsset() == "") {
            context.path()
        } else {
            context.redirectAsset()
        }
        val assetFile = File("${this.path}/${assetName}")
        if (assetFile.isFile && assetFile.exists()) {
            return KoraAsset(assetFile)
        }
        HttpPathNotRegisteredException.notFound(assetName, "auto redirect")
    }

    fun response(context: KoraHttpContext, asset: KoraAsset?): ByteArray {
        if (asset != null) {
            val data = asset.data
            context.withContentType(
                KoraHttpFileExtentions.getContentType(
                    asset.file
                )
            )
            return data
        }
        HttpPathNotRegisteredException.notFound(context.path())
    }

    fun response(context: KoraHttpContext): ByteArray {
        return response(context, getAsset(context))
    }
}