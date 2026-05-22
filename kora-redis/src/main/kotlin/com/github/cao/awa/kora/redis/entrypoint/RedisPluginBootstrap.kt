package com.github.cao.awa.kora.redis.entrypoint

import com.github.cao.awa.com.github.cao.awa.kora.redis.KoraRedisClient
import com.github.cao.awa.com.github.cao.awa.kora.redis.config.KoraRedisClientConfig
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File

object RedisPluginBootstrap {
    private val LOGGER: Logger = LogManager.getLogger("RedisPluginBootstrap")

    @JvmStatic
    fun init() {
        LOGGER.info("Initializing redis client")
        val configFile = File("configs/redis_client.json")

        KoraRedisClient.init(KoraRedisClientConfig.createConfig(configFile))
    }
}