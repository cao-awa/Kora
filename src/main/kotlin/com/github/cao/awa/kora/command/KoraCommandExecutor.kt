package com.github.cao.awa.kora.command

import com.github.cao.awa.kora.status.KoraStatus.reload
import com.github.cao.awa.kora.status.KoraStatus.stop
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object KoraCommandExecutor {
    private val LOGGER: Logger = LogManager.getLogger("KoraCommandExecutor")
    private val RUNTIME: Runtime = Runtime.getRuntime()

    @JvmStatic
    fun executeCommand(command: String) {
        when (command) {
            "stop", "exit" -> stop()
            "reload" -> reload()
            "mem", "memory" -> {
                LOGGER.info("-- Memory --")
                LOGGER.info("Total memory: {}MB", RUNTIME.totalMemory() / 1024 / 1024)
                LOGGER.info("Max memory: {}MB", RUNTIME.maxMemory() / 1024 / 1024)
                LOGGER.info("Free memory: {}MB", RUNTIME.freeMemory() / 1024 / 1024)
            }
            else -> LOGGER.info(
                "Unknown command: {}",
                command
            )
        }
    }
}