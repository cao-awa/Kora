package com.github.cao.awa.kora.server.network.http.config

import com.github.cao.awa.kora.server.network.config.KoraNettyServerConfig
import com.github.cao.awa.kora.server.network.http.asset.config.KoraAssetManagerConfig
import io.netty.buffer.ByteBufAllocator
import io.netty.channel.WriteBufferWaterMark

object KoraHttpDefaultServerConfig: KoraHttpServerConfig() {
    private fun throwWhenSet(): Nothing {
        error("Cannot set config in default server config instance")
    }

    override fun serverPort(port: Int): KoraHttpServerConfig {
        throwWhenSet()
    }

    override fun serverHost(host: String): KoraHttpServerConfig {
        throwWhenSet()
    }

    override fun assetManagerConfig(config: KoraAssetManagerConfig): KoraHttpServerConfig {
        throwWhenSet()
    }

    override fun nettyServerConfig(config: KoraNettyServerConfig<*>): KoraHttpServerConfig {
        throwWhenSet()
    }

    override fun backlog(backlog: Int): KoraHttpServerConfig {
        throwWhenSet()
    }

    override fun keepalive(keepalive: Boolean): KoraHttpServerConfig {
        throwWhenSet()
    }

    override fun tcpNoDelay(noDelay: Boolean): KoraHttpServerConfig {
        throwWhenSet()
    }

    override fun rcvBuf(rcvBuf: Int): KoraHttpServerConfig {
        throwWhenSet()
    }

    override fun reuseAddr(reuseAddr: Boolean): KoraHttpServerConfig {
        throwWhenSet()
    }

    override fun allocator(allocator: ByteBufAllocator): KoraHttpServerConfig {
        throwWhenSet()
    }

    override fun writeBufferWaterMark(waterMark: WriteBufferWaterMark): KoraHttpServerConfig {
        throwWhenSet()
    }
}