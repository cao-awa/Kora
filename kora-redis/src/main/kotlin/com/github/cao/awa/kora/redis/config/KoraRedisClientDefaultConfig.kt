package com.github.cao.awa.com.github.cao.awa.kora.redis.config

object KoraRedisClientDefaultConfig: KoraRedisClientConfig() {
    private fun throwWhenSet() {
        error("Cannot set config in default server config instance")
    }

    override fun host(host: String): KoraRedisClientConfig {
        throwWhenSet()
        return this
    }

    override fun port(port: Int): KoraRedisClientConfig {
        throwWhenSet()
        return this
    }
}