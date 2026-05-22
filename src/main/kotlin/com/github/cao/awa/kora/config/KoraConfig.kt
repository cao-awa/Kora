package com.github.cao.awa.kora.config

import com.github.cao.awa.cason.obj.JSONObject
import com.github.cao.awa.cason.serialize.parser.JSONParser
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File

abstract class KoraConfig {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger("KoraConfig")
        private val EMPTY_CONFIG: JSONObject = JSONObject()

        fun <T: KoraConfig> createConfig(file: File, builder: JSONObject.() -> T): T {
            val config: T =
            if (file.isFile) {
                LOGGER.info("Creating config from file '{}'", file.absolutePath)
                builder(JSONParser.parseObject(file.readText(Charsets.UTF_8)))
            } else {
                LOGGER.info("Config not found, creating config to file '{}'", file.absolutePath)
                file.parentFile.let {
                    if (!it.exists()) {
                        it.mkdirs()
                    }
                }
                builder(EMPTY_CONFIG)
            }
            file.writeText(config.toJSON().toString(true, "    "))
            return config
        }

        fun <T: KoraConfig> createConfig(json: JSONObject, builder: JSONObject.() -> T): T {
            return builder(json)
        }
    }

    abstract fun toJSON(): JSONObject
}