package com.github.cao.awa.kora.external

import com.github.cao.awa.kora.launch.config.KoraLaunchConfig
import com.github.cao.awa.kora.plugin.markPluginLoaded
import com.github.cao.awa.kora.redis.client.KoraRedisClient
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object SampleEntrypoint {
    private val LOGGER: Logger = LogManager.getLogger("SampleEntrypoint")
    val NAME: String = "kora-external"

    @JvmStatic
    fun entry(launchConfig: KoraLaunchConfig) {
        LOGGER.info("External test success!")

        LOGGER.info("Testing redis client...")

        val client = KoraRedisClient.INSTANCE
        LOGGER.info("Set key 'test_redis' to 'awa'")
        client["test_redis"] = "awa"
        val result = client["test_redis"]
        LOGGER.info("Get key 'test_redis': $result")

        markPluginLoaded(NAME)
    }

    @JvmStatic
    fun fallback(launchConfig: KoraLaunchConfig) {
        LOGGER.info("Fallback test success!")
    }
}