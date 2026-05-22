package com.github.cao.awa.kora.server.network.group

import com.google.common.util.concurrent.ThreadFactoryBuilder
import io.netty.channel.*
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollIoHandler
import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.channel.kqueue.KQueue
import io.netty.channel.kqueue.KQueueIoHandler
import io.netty.channel.kqueue.KQueueServerSocketChannel
import io.netty.channel.local.LocalIoHandler
import io.netty.channel.local.LocalServerChannel
import io.netty.channel.nio.NioIoHandler
import io.netty.channel.socket.nio.NioServerSocketChannel
import java.util.concurrent.ThreadFactory

abstract class KoraEventLoopGroupFactory internal constructor(
    private val name: String,
    val channel: Class<out ServerChannel>
) {
    companion object {
        private val THREAD_FACTORY: ThreadFactory = Thread.ofPlatform().factory()
        private val NIO: KoraEventLoopGroupFactory =
            object :
                KoraEventLoopGroupFactory("NIO", NioServerSocketChannel::class.java) {
                override fun newIoHandlerFactory(): IoHandlerFactory = NioIoHandler.newFactory()
            }
        private val EPOLL: KoraEventLoopGroupFactory =
            object : KoraEventLoopGroupFactory("Epoll", EpollServerSocketChannel::class.java) {
                override fun newIoHandlerFactory(): IoHandlerFactory = EpollIoHandler.newFactory()
            }
        private val KQUEUE: KoraEventLoopGroupFactory =
            object : KoraEventLoopGroupFactory("Kqueue", KQueueServerSocketChannel::class.java) {
                override fun newIoHandlerFactory(): IoHandlerFactory = KQueueIoHandler.newFactory()
            }
        private val LOCAL: KoraEventLoopGroupFactory =
            object : KoraEventLoopGroupFactory("Local", LocalServerChannel::class.java) {
                override fun newIoHandlerFactory(): IoHandlerFactory = LocalIoHandler.newFactory()
            }

        fun remote(): KoraEventLoopGroupFactory {
            if (Epoll.isAvailable()) {
                return EPOLL
            }
            if (KQueue.isAvailable()) {
                return KQUEUE
            }
            return NIO
        }

        fun nio(): KoraEventLoopGroupFactory = NIO

        fun epoll(): KoraEventLoopGroupFactory = EPOLL

        fun kqueue(): KoraEventLoopGroupFactory = KQUEUE

        fun local(): KoraEventLoopGroupFactory = LOCAL

        fun validate(io: KoraEventLoopGroupFactory): KoraEventLoopGroupFactory {
            if (io == local()) {
                return io
            }

            if (io == epoll() && Epoll.isAvailable()) {
                return io
            }

            if (io == kqueue() && KQueue.isAvailable()) {
                return io
            }

            return NIO
        }
    }

    protected abstract fun newIoHandlerFactory(): IoHandlerFactory

    private fun createThreadFactory(): ThreadFactory {
        return ThreadFactoryBuilder()
            .setNameFormat("Netty ${this.name} / #%d")
            .setThreadFactory(THREAD_FACTORY)
            .setDaemon(true)
            .build()
    }

    fun createEventLoopGroup(count: Int = 1): EventLoopGroup {
        synchronized(this) {
            return MultiThreadIoEventLoopGroup(count, createThreadFactory(), newIoHandlerFactory())
        }
    }
}
