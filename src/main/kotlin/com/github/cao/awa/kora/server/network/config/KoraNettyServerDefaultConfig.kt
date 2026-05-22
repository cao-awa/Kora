package com.github.cao.awa.kora.server.network.config

import com.github.cao.awa.kora.server.network.group.KoraEventLoopGroupFactory
import io.netty.buffer.ByteBufAllocator
import io.netty.channel.WriteBufferWaterMark

object KoraNettyServerDefaultConfig: KoraNettyServerConfig<KoraNettyServerDefaultConfig>() {
    private fun throwWhenSet() {
        error("Cannot set config in default server config instance")
    }

    override fun io(io: KoraEventLoopGroupFactory): KoraNettyServerConfig<KoraNettyServerDefaultConfig> {
        throwWhenSet()
        return this
    }

    override fun backlog(backlog: Int): KoraNettyServerConfig<KoraNettyServerDefaultConfig> {
        throwWhenSet()
        return this
    }

    override fun keepalive(keepalive: Boolean): KoraNettyServerConfig<KoraNettyServerDefaultConfig> {
        throwWhenSet()
        return this
    }

    override fun rcvBuf(rcvBuf: Int): KoraNettyServerConfig<KoraNettyServerDefaultConfig> {
        throwWhenSet()
        return this
    }

    override fun reuseAddr(reuseAddr: Boolean): KoraNettyServerConfig<KoraNettyServerDefaultConfig> {
        throwWhenSet()
        return this
    }

    override fun allocator(allocator: ByteBufAllocator): KoraNettyServerConfig<KoraNettyServerDefaultConfig> {
        throwWhenSet()
        return this
    }

    override fun writeBufferWaterMark(waterMark: WriteBufferWaterMark): KoraNettyServerConfig<KoraNettyServerDefaultConfig> {
        throwWhenSet()
        return this
    }

    override fun tcpNoDelay(noDelay: Boolean): KoraNettyServerConfig<KoraNettyServerDefaultConfig> {
        throwWhenSet()
        return this
    }
}