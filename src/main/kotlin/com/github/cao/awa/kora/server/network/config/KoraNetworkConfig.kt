package com.github.cao.awa.kora.server.network.config

import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.PooledByteBufAllocator
import io.netty.channel.WriteBufferWaterMark

abstract class KoraNetworkConfig<T: KoraNetworkConfig<T>> {
    private var backlog: Int = 2048
    private var keepalive: Boolean = true
    private var rcvBuf: Int = 65536
    private var reuseAddr: Boolean = true
    private var allocator: ByteBufAllocator = PooledByteBufAllocator.DEFAULT
    private var writeBufferWaterMark: WriteBufferWaterMark = WriteBufferWaterMark(
        32 * 1024,
        64 * 1024
    )

    fun backlog(): Int = this.backlog

    open fun backlog(backlog: Int): KoraNetworkConfig<T> {
        this.backlog = backlog
        return this
    }

    fun keepalive(): Boolean = this.keepalive

    open fun keepalive(keepalive: Boolean): KoraNetworkConfig<T> {
        this.keepalive = keepalive
        return this
    }


    fun rcvBuf(): Int = this.rcvBuf

    open fun rcvBuf(rcvBuf: Int): KoraNetworkConfig<T> {
        this.rcvBuf = rcvBuf
        return this
    }

    fun reuseAddr(): Boolean = this.reuseAddr

    open fun reuseAddr(reuseAddr: Boolean): KoraNetworkConfig<T> {
        this.reuseAddr = reuseAddr
        return this
    }

    fun allocator(): ByteBufAllocator = this.allocator

    open fun allocator(allocator: ByteBufAllocator): KoraNetworkConfig<T> {
        this.allocator = allocator
        return this
    }

    fun writeBufferWaterMark(): WriteBufferWaterMark = this.writeBufferWaterMark

    open fun writeBufferWaterMark(waterMark: WriteBufferWaterMark): KoraNetworkConfig<T> {
        this.writeBufferWaterMark = waterMark
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

    abstract fun copy(): T
}