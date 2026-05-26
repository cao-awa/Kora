package com.github.cao.awa.kora.server.network.config

import com.github.cao.awa.cason.obj.JSONObject
import com.github.cao.awa.kora.config.KoraConfig
import com.github.cao.awa.kora.server.network.group.KoraEventLoopGroupFactory
import com.github.cao.awa.kora.server.network.http.config.KoraHttpServerConfig
import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.PooledByteBufAllocator
import io.netty.buffer.UnpooledByteBufAllocator
import io.netty.channel.WriteBufferWaterMark
import io.netty.channel.unix.PreferredDirectByteBufAllocator

abstract class KoraNettyServerConfig<T : KoraNettyServerConfig<T>> : KoraConfig() {
    companion object {
        fun createFromJSON(json: JSONObject): KoraHttpServerConfig {
            return createConfig(json) {
                val config = KoraHttpServerConfig()
                ifString("io") {
                    config.io(
                        when (this) {
                            "default", "epoll" -> KoraEventLoopGroupFactory.epoll()
                            "nio" -> KoraEventLoopGroupFactory.nio()
                            "kqueue" -> KoraEventLoopGroupFactory.kqueue()
                            "local" -> KoraEventLoopGroupFactory.local()
                            "io_uring" -> KoraEventLoopGroupFactory.ioUring()
                            else -> throw IllegalArgumentException("No such io event loop group factory: '$this'")
                        }
                    )
                }
                ifInt("backlog") {
                    config.backlog(this)
                }
                ifBoolean("keep_alive") {
                    config.keepalive(this)
                }
                ifInt("rcv_buffer") {
                    config.rcvBuf(this)
                }
                ifInt("snd_buffer") {
                    config.sndBuf(this)
                }
                ifBoolean("reuse_address") {
                    config.reuseAddr(this)
                }
                ifBoolean("tcp_no_delay") {
                    config.tcpNoDelay(this)
                }
                ifString("allocator") {
                    config.allocator(
                        when (this) {
                            "default", "pooled" -> PooledByteBufAllocator.DEFAULT
                            "unpolled" -> UnpooledByteBufAllocator.DEFAULT
                            "direct" -> PreferredDirectByteBufAllocator.DEFAULT
                            else -> throw IllegalArgumentException("Unknown allocator type: $this")
                        }
                    )
                }

                config
            }
        }
    }

    private var io: KoraEventLoopGroupFactory = KoraEventLoopGroupFactory.epoll()
    private var backlog: Int = 8192
    private var keepalive: Boolean = true
    private var rcvBuf: Int = 65536
    private var reuseAddr: Boolean = true
    private var sndBuf: Int = 65536
    private var allocator: ByteBufAllocator = PooledByteBufAllocator.DEFAULT
    private var writeBufferWaterMark: WriteBufferWaterMark = WriteBufferWaterMark(
        32 * 1024 * 1024,
        64 * 1024 * 1024
    )
    private var tcpNoDelay: Boolean = true

    fun io(): KoraEventLoopGroupFactory = this.io

    fun ioName(): String {
        return when (KoraEventLoopGroupFactory.validate(this.io)) {
            KoraEventLoopGroupFactory.epoll() -> "epoll"
            KoraEventLoopGroupFactory.nio() -> "nio"
            KoraEventLoopGroupFactory.kqueue() -> "kqueue"
            KoraEventLoopGroupFactory.local() -> "local"
            KoraEventLoopGroupFactory.ioUring() -> "io_uring"
            else -> "default"
        }
    }

    open fun io(io: KoraEventLoopGroupFactory): KoraNettyServerConfig<T> {
        this.io = io
        return this
    }

    fun backlog(): Int = this.backlog

    open fun backlog(backlog: Int): KoraNettyServerConfig<T> {
        this.backlog = backlog
        return this
    }

    fun keepalive(): Boolean = this.keepalive

    open fun keepalive(keepalive: Boolean): KoraNettyServerConfig<T> {
        this.keepalive = keepalive
        return this
    }


    fun rcvBuf(): Int = this.rcvBuf

    open fun rcvBuf(rcvBuf: Int): KoraNettyServerConfig<T> {
        this.rcvBuf = rcvBuf
        return this
    }

    fun sndBuf(): Int = this.sndBuf

    open fun sndBuf(sndBuf: Int): KoraNettyServerConfig<T> {
        this.sndBuf = sndBuf
        return this
    }

    fun reuseAddr(): Boolean = this.reuseAddr

    open fun reuseAddr(reuseAddr: Boolean): KoraNettyServerConfig<T> {
        this.reuseAddr = reuseAddr
        return this
    }

    fun allocator(): ByteBufAllocator = this.allocator

    open fun allocator(allocator: ByteBufAllocator): KoraNettyServerConfig<T> {
        this.allocator = allocator
        return this
    }

    fun allocatorName(): String {
        return when (this.allocator) {
            is PooledByteBufAllocator -> "default"
            is UnpooledByteBufAllocator -> "unpooled"
            is PreferredDirectByteBufAllocator -> "direct"
            else -> "default"
        }
    }

    fun writeBufferWaterMark(): WriteBufferWaterMark = this.writeBufferWaterMark

    open fun writeBufferWaterMark(waterMark: WriteBufferWaterMark): KoraNettyServerConfig<T> {
        this.writeBufferWaterMark = waterMark
        return this
    }

    fun tcpNoDelay(): Boolean = this.tcpNoDelay

    open fun tcpNoDelay(noDelay: Boolean): KoraNettyServerConfig<T> {
        this.tcpNoDelay = noDelay
        return this
    }

    fun copy(instance: T): T {
        instance.io(io())
        instance.backlog(backlog())
        instance.keepalive(keepalive())
        instance.rcvBuf(rcvBuf())
        instance.reuseAddr(reuseAddr())
        instance.allocator(allocator())
        instance.writeBufferWaterMark(writeBufferWaterMark())
        return instance
    }

    fun copy(config: KoraNettyServerConfig<*>) {
        config.io = this.io
        config.backlog = this.backlog
        config.keepalive = this.keepalive
        config.reuseAddr = this.reuseAddr
        config.tcpNoDelay = this.tcpNoDelay
        config.allocator = this.allocator
        config.writeBufferWaterMark = this.writeBufferWaterMark
    }

    override fun toJSON(): JSONObject {
        return JSONObject {
            "io" set ioName()
            "backlog" set backlog
            "keep_alive" set keepalive
            "rcv_buffer" set rcvBuf
            "snd_buffer" set sndBuf
            "reuse_address" set reuseAddr
            "tcp_no_delay" set tcpNoDelay
            "allocator" set allocatorName()
        }
    }
}