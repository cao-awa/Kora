package com.github.cao.awa.kora.server.network.config

import com.github.cao.awa.cason.obj.JSONObject
import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.PooledByteBufAllocator
import io.netty.channel.WriteBufferWaterMark

abstract class KoraNettyConfig<T: KoraNettyConfig<T>> {
    private var useEpoll: Boolean = true
    private var backlog: Int = 8192
    private var keepalive: Boolean = true
    private var rcvBuf: Int = 65536
    private var reuseAddr: Boolean = true
    private var allocator: ByteBufAllocator = PooledByteBufAllocator.DEFAULT
    private var writeBufferWaterMark: WriteBufferWaterMark = WriteBufferWaterMark(
        32 * 1024 * 1024,
        64 * 1024 * 1024
    )
    private var tcpNoDelay: Boolean = true

    fun useEpoll(): Boolean = this.useEpoll

    open fun useEpoll(useEpoll: Boolean): KoraNettyConfig<T> {
        this.useEpoll = useEpoll
        return this
    }

    fun backlog(): Int = this.backlog

    open fun backlog(backlog: Int): KoraNettyConfig<T> {
        this.backlog = backlog
        return this
    }

    fun keepalive(): Boolean = this.keepalive

    open fun keepalive(keepalive: Boolean): KoraNettyConfig<T> {
        this.keepalive = keepalive
        return this
    }


    fun rcvBuf(): Int = this.rcvBuf

    open fun rcvBuf(rcvBuf: Int): KoraNettyConfig<T> {
        this.rcvBuf = rcvBuf
        return this
    }

    fun reuseAddr(): Boolean = this.reuseAddr

    open fun reuseAddr(reuseAddr: Boolean): KoraNettyConfig<T> {
        this.reuseAddr = reuseAddr
        return this
    }

    fun allocator(): ByteBufAllocator = this.allocator

    open fun allocator(allocator: ByteBufAllocator): KoraNettyConfig<T> {
        this.allocator = allocator
        return this
    }

    fun writeBufferWaterMark(): WriteBufferWaterMark = this.writeBufferWaterMark

    open fun writeBufferWaterMark(waterMark: WriteBufferWaterMark): KoraNettyConfig<T> {
        this.writeBufferWaterMark = waterMark
        return this
    }

    fun tcpNoDelay(): Boolean = this.tcpNoDelay

    open fun tcpNoDelay(noDelay: Boolean): KoraNettyConfig<T> {
        this.tcpNoDelay = noDelay
        return this
    }

    fun copy(instance: T): T {
        instance.backlog(backlog())
        instance.keepalive(keepalive())
        instance.rcvBuf(rcvBuf())
        instance.reuseAddr(reuseAddr())
        instance.allocator(allocator())
        instance.writeBufferWaterMark(writeBufferWaterMark())
        return instance
    }

    open fun toJSON(): JSONObject {
        return JSONObject {
            "backlog" set backlog
            "keep_alive" set keepalive
            "rcv_buffer" set rcvBuf
            "reuse_address" set reuseAddr
            "tcp_noDelay" set tcpNoDelay
        }
    }

    abstract fun copy(): T
}