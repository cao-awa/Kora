package com.github.cao.awa.kora.server.network.config

import com.github.cao.awa.kora.server.network.group.KoraEventLoopGroupFactory
import io.netty.buffer.ByteBufAllocator
import io.netty.channel.WriteBufferWaterMark

object KoraNettyServerDefaultConfig: KoraNettyServerConfig() {
    private fun throwWhenSet(): Nothing {
        error("Cannot set config in default server config instance")
    }

    override fun io(io: KoraEventLoopGroupFactory): KoraNettyServerConfig {
        throwWhenSet()
    }

    override fun backlog(backlog: Int): KoraNettyServerConfig {
        throwWhenSet()
    }

    override fun keepalive(keepalive: Boolean): KoraNettyServerConfig {
        throwWhenSet()
    }

    override fun rcvBuf(rcvBuf: Int): KoraNettyServerConfig {
        throwWhenSet()
    }

    override fun sndBuf(sndBuf: Int): KoraNettyServerConfig {
        throwWhenSet()
    }

    override fun reuseAddr(reuseAddr: Boolean): KoraNettyServerConfig {
        throwWhenSet()
    }

    override fun allocator(allocator: ByteBufAllocator): KoraNettyServerConfig {
        throwWhenSet()
    }

    override fun writeBufferWaterMark(waterMark: WriteBufferWaterMark): KoraNettyServerConfig {
        throwWhenSet()
    }

    override fun tcpNoDelay(noDelay: Boolean): KoraNettyServerConfig {
        throwWhenSet()
    }
}