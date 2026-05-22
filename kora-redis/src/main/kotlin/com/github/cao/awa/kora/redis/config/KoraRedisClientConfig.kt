package com.github.cao.awa.com.github.cao.awa.kora.redis.config

import com.github.cao.awa.cason.obj.JSONObject
import com.github.cao.awa.kora.config.KoraConfig
import java.io.File

open class KoraRedisClientConfig: KoraConfig() {
    companion object {
        fun createConfig(file: File): KoraRedisClientConfig {
            return createConfig(file) {
                val config = KoraRedisClientConfig()

                ifString("host") {
                    config.host(this)
                }

                ifInt("port") {
                    config.port = this
                }

                config
            }
        }
    }

    private var host: String = "127.0.0.1"
    private var port: Int = 6379

    fun host(): String {
        return this.host
    }

    open fun host(host: String): KoraRedisClientConfig {
        this.host = host
        return this
    }

    fun port(): Int {
        return this.port
    }

    open fun port(port: Int): KoraRedisClientConfig {
        this.port = port
        return this
    }

    override fun toJSON(): JSONObject {
        return JSONObject {
            "host" set host
            "port" set port
        }
    }
}