package com.github.cao.awa.kora.launch.config

import com.github.cao.awa.cason.obj.JSONObject
import com.github.cao.awa.cason.primary.JSONString
import com.github.cao.awa.kora.config.KoraConfig
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.util.LinkedList

open class KoraLaunchConfig: KoraConfig() {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger("KoraLaunchConfig")

        @JvmStatic
        fun createConfig(file: File): KoraLaunchConfig {
            return createConfig(file)  {
                val config = KoraLaunchConfig()
                ifBoolean("print_config_details") {
                    config.printConfigDetails = this
                }
                ifString("entrypoint") {
                    if (this != "") {
                        config.entrypoint.clear()
                        config.entrypoint.add(this)
                    }
                }
                ifArray("entrypoint") {
                    if (!isEmpty()) {
                        config.entrypoint.clear()
                        forEach { entrypoint ->
                            if (!entrypoint.isString()) {
                                throw IllegalArgumentException("Entrypoint definition must be string")
                            }
                            if (entrypoint is JSONString) {
                                config.entrypoint.add(entrypoint.asString())
                            }
                        }
                    } else{
                        LOGGER.warn("Entrypoint definition is empty, will use default entrypoint 'com.github.cao.awa.kora.server.network.http.entrypoint.KoraHttpServerEntrypoint#entry'")
                    }
                }

                config
            }
        }
    }

    private var printConfigDetails: Boolean = true
    private var entrypoint: LinkedList<String> =
        LinkedList<String>().also {
            it.add("com.github.cao.awa.kora.server.network.http.entrypoint.KoraHttpServerEntrypoint#entry")
        }
    private var error: Throwable? = null
    private var sharedContext: MutableMap<String, String> = mutableMapOf()

    fun printConfigDetails(): Boolean {
        return this.printConfigDetails
    }

    open fun printConfigDetails(print: Boolean): KoraLaunchConfig {
        this.printConfigDetails = print
        return this
    }

    fun entrypoint(): LinkedList<String> {
        return this.entrypoint
    }

    open fun entrypoint(entrypoint: LinkedList<String>): KoraLaunchConfig {
        this.entrypoint = entrypoint
        return this
    }

    fun error(): Throwable {
        return this.error!!
    }

    open fun error(error: Throwable): KoraLaunchConfig {
        this.error = error
        return this
    }

    fun resetError(): KoraLaunchConfig {
        this.error = null
        return this
    }

    operator fun set(key: String, data: String) {
        this.sharedContext[key] = data
    }

    operator fun get(key: String): String? {
        return this.sharedContext[key]
    }

    override fun toJSON(): JSONObject {
        return JSONObject {
            "print_config_details" set printConfigDetails
            arr("entrypoint") {
                for (entry in entrypoint) {
                    +entry
                }
            }
        }
    }
}