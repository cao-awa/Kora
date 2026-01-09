package com.github.cao.awa.kora.server.network.group

import com.google.common.util.concurrent.ThreadFactoryBuilder
import io.netty.channel.*
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollIoHandler
import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.channel.epoll.EpollSocketChannel
import io.netty.channel.kqueue.KQueue
import io.netty.channel.kqueue.KQueueIoHandler
import io.netty.channel.kqueue.KQueueServerSocketChannel
import io.netty.channel.kqueue.KQueueSocketChannel
import io.netty.channel.local.LocalChannel
import io.netty.channel.local.LocalIoHandler
import io.netty.channel.local.LocalServerChannel
import io.netty.channel.nio.NioIoHandler
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory

abstract class KoraEventLoopGroupFactory internal constructor(
    private val name: String,
    val channel: Class<out ServerChannel>
) {
    companion object {
        private val THREAD_FACTORY: ThreadFactory = Thread.ofVirtual().factory()
        private val NIO: KoraEventLoopGroupFactory =
            object :
                KoraEventLoopGroupFactory("NIO", NioServerSocketChannel::class.java) {
                override fun newFactory(): IoHandlerFactory = NioIoHandler.newFactory()
            }
        private val EPOLL: KoraEventLoopGroupFactory =
            object : KoraEventLoopGroupFactory("Epoll", EpollServerSocketChannel::class.java) {
                override fun newFactory(): IoHandlerFactory = EpollIoHandler.newFactory()
            }
        private val KQUEUE: KoraEventLoopGroupFactory =
            object : KoraEventLoopGroupFactory("Kqueue", KQueueServerSocketChannel::class.java) {
                override fun newFactory(): IoHandlerFactory = KQueueIoHandler.newFactory()
            }
        private val LOCAL: KoraEventLoopGroupFactory =
            object : KoraEventLoopGroupFactory("Local", LocalServerChannel::class.java) {
                override fun newFactory(): IoHandlerFactory = LocalIoHandler.newFactory()
            }

        fun remote(useEpoll: Boolean): KoraEventLoopGroupFactory {
            if (useEpoll) {
                if (KQueue.isAvailable()) {
                    return KQUEUE
                }

                if (Epoll.isAvailable()) {
                    return EPOLL
                }
            }
            return NIO
        }

        fun local(): KoraEventLoopGroupFactory = LOCAL
    }

    protected abstract fun newFactory(): IoHandlerFactory

    private fun createThreadFactory(): ThreadFactory {
        return ThreadFactoryBuilder()
            .setNameFormat("Netty ${this.name} / #%d")
            .setThreadFactory(THREAD_FACTORY)
            .setDaemon(true)
            .build()
    }

    fun createEventLoopGroup(): EventLoopGroup {
        synchronized(this) {
            val threadFactory: ThreadFactory = createThreadFactory()
            return MultiThreadIoEventLoopGroup(threadFactory, newFactory())
        }
    }
}
