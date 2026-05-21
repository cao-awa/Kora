package com.github.cao.awa.kora.server.network.config

import com.github.cao.awa.cason.obj.JSONObject
import com.github.cao.awa.kora.server.network.http.config.KoraHttpServerConfig
import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.PooledByteBufAllocator
import io.netty.channel.WriteBufferWaterMark

abstract class KoraNettyServerConfig<T : KoraNettyServerConfig<T>> {
    companion object {
        fun createFromJSON(json: JSONObject): KoraHttpServerConfig {
            val config = KoraHttpServerConfig()
            json.getBoolean("use_epoll")?.let { useEpoll ->
                config.useEpoll(useEpoll)
            }
            json.getInt("backlog")?.let {
                config.backlog(it)
            }
            json.getBoolean("keep_alive")?.let {
                config.keepalive(it)
            }
            json.getInt("rcv_buffer")?.let {
                config.rcvBuf(it)
            }
            json.getBoolean("reuse_address")?.let {
                config.reuseAddr(it)
            }
            json.getBoolean("tcp_no_delay")?.let {
                config.tcpNoDelay(it)
            }
            json.getString("allocator")?.let { allocator ->
                config.allocator(
                    when (allocator) {
                        "default" -> PooledByteBufAllocator.DEFAULT
                        else -> throw IllegalArgumentException("Unknown allocator type: $allocator")
                    }
                )
            }
            return config
        }
    }

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

    open fun useEpoll(useEpoll: Boolean): KoraNettyServerConfig<T> {
        this.useEpoll = useEpoll
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
        return when(this.allocator) {
            PooledByteBufAllocator.DEFAULT -> "default"
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
        instance.useEpoll(useEpoll())
        instance.backlog(backlog())
        instance.keepalive(keepalive())
        instance.rcvBuf(rcvBuf())
        instance.reuseAddr(reuseAddr())
        instance.allocator(allocator())
        instance.writeBufferWaterMark(writeBufferWaterMark())
        return instance
    }

    fun copy(config: KoraNettyServerConfig<*>) {
        config.useEpoll = this.useEpoll
        config.backlog = this.backlog
        config.keepalive = this.keepalive
        config.reuseAddr = this.reuseAddr
        config.tcpNoDelay = this.tcpNoDelay
        config.allocator = this.allocator
        config.writeBufferWaterMark = this.writeBufferWaterMark
    }

    open fun toJSON(): JSONObject {
        return JSONObject {
            "use_epoll" to useEpoll
            "backlog" set backlog
            "keep_alive" set keepalive
            "rcv_buffer" set rcvBuf
            "reuse_address" set reuseAddr
            "tcp_no_delay" set tcpNoDelay
            "allocator" set when (allocator) {
                PooledByteBufAllocator.DEFAULT -> "default"
                else -> "default"
            }
        }
    }
}