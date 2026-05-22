package com.github.cao.awa.kora.server.network.http.asset.config

import com.github.cao.awa.cason.obj.JSONObject

open class KoraAssetManagerConfig {
    companion object {
        @JvmStatic
        fun createFromJSON(json: JSONObject): KoraAssetManagerConfig {
            val config = KoraAssetManagerConfig()
            json.getBoolean("enable")?.let { enable ->
                config.enable = enable
            }
            json.getString("asset_path")?.let { assetPath ->
                config.assetPath = assetPath
            }
            json.getString("error_page")?.let { errorPage ->
                config.errorPage = errorPage
            }
            return config
        }
    }

    private var enable: Boolean = true
    private var assetPath: String = "assets/"
    private var errorPage: String = ""

    fun enable(): Boolean {
        return this.enable
    }

    open fun enable(enabled: Boolean): KoraAssetManagerConfig {
        this.enable = enabled
        return this
    }

    fun assetPath(): String {
        return this.assetPath
    }

    open fun assetPath(assetPath: String): KoraAssetManagerConfig {
        this.assetPath = assetPath
        return this
    }

    fun errorPage(): String {
        return this.errorPage
    }

    open fun errorPage(errorPage: String): KoraAssetManagerConfig {
        this.errorPage = errorPage
        return this;
    }

    fun toJSON(): JSONObject {
        return JSONObject {
            "enable" to enable
            "asset_path" set assetPath
            "error_page" set errorPage
        }
    }
}