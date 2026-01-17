package com.github.cao.awa.kora.server.network.http.config

import io.netty.buffer.ByteBufAllocator
import io.netty.channel.WriteBufferWaterMark

object KoraHttpDefaultServerConfig: KoraHttpServerConfig() {
    private fun throwWhenSet() {
        error("Cannot set config in default server config instance")
    }

    override fun backlog(backlog: Int): KoraHttpServerConfig {
        throwWhenSet()
        return this
    }

    override fun keepalive(keepalive: Boolean): KoraHttpServerConfig {
        throwWhenSet()
        return this
    }

    override fun tcpNoDelay(tcpNoDelay: Boolean): KoraHttpServerConfig {
        throwWhenSet()
        return this
    }

    override fun rcvBuf(rcvBuf: Int): KoraHttpServerConfig {
        throwWhenSet()
        return this
    }

    override fun reuseAddr(reuseAddr: Boolean): KoraHttpServerConfig {
        throwWhenSet()
        return this
    }

    override fun allocator(allocator: ByteBufAllocator): KoraHttpServerConfig {
        throwWhenSet()
        return this
    }

    override fun writeBufferWaterMark(waterMark: WriteBufferWaterMark): KoraHttpServerConfig {
        throwWhenSet()
        return this
    }
}