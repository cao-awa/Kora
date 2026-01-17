package com.github.cao.awa.kora.server.network.http

import com.github.cao.awa.kora.constant.KoraInformation
import com.github.cao.awa.kora.server.network.group.KoraEventLoopGroupFactory
import com.github.cao.awa.kora.server.network.http.builder.KoraHttpServerBuilder
import com.github.cao.awa.kora.server.network.http.adapter.KoraHttpInboundHandlerAdapter
import com.github.cao.awa.kora.server.network.http.config.KoraHttpDefaultServerConfig
import com.github.cao.awa.kora.server.network.http.config.KoraHttpServerConfig
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.PooledByteBufAllocator
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.WriteBufferWaterMark
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpRequestDecoder
import io.netty.handler.codec.http.HttpResponseEncoder

class KoraHttpServer {
    companion object {
        var instructHttpMetadata: Boolean = true
        var instructHttpStatusCode: Boolean = true
        var instructHttpVersionCode: Boolean = true
        var fastAbort: Boolean = false
    }

    private val serverBuilder: KoraHttpServerBuilder

    constructor(builder: KoraHttpServerBuilder) {
        this.serverBuilder = builder
    }

    fun start(
        port: Int,
        address: String = "localhost",
        useEpoll: Boolean = true,
        config: KoraHttpServerConfig = KoraHttpDefaultServerConfig
    ) {
        val threadFactory = KoraEventLoopGroupFactory.remote(
            useEpoll
        )
        val bossGroup: EventLoopGroup = threadFactory.createEventLoopGroup(1)
        val workerGroup: EventLoopGroup =
            threadFactory.createEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2)
        try {
            val bootstrap = ServerBootstrap()
                .group(
                    bossGroup,
                    workerGroup
                ).channel(
                    threadFactory.channel
                ).option(
                    ChannelOption.SO_BACKLOG, config.backlog()
                ).childOption(
                    ChannelOption.TCP_NODELAY, config.tcpNoDelay()
                ).childOption(
                    ChannelOption.SO_KEEPALIVE, config.keepalive()
                ).childOption(
                    ChannelOption.SO_RCVBUF, config.rcvBuf()
                ).childOption(
                    ChannelOption.SO_REUSEADDR, config.reuseAddr()
                ).childOption(
                    ChannelOption.WRITE_BUFFER_WATER_MARK, config.writeBufferWaterMark()
                ).childOption(
                    ChannelOption.ALLOCATOR, config.allocator()
                ).childHandler(object : ChannelInitializer<SocketChannel>() {
                    @Override
                    override fun initChannel(channel: SocketChannel) {
                        channel.pipeline().apply {
                            addLast(HttpRequestDecoder())
                            addLast(HttpResponseEncoder())
                            // Only aggregate 1MB http request.
                            addLast(HttpObjectAggregator(KoraInformation.MB))
                            addLast(KoraHttpInboundHandlerAdapter(this@KoraHttpServer.serverBuilder))
                        }
                    }
                })

            val future = bootstrap.bind(
                address,
                port
            ).sync()
            println("Kora HTTP server started on port $port on $address")
            future.channel().closeFuture().sync()
        } finally {
            bossGroup.shutdownGracefully()
            workerGroup.shutdownGracefully()
        }
    }
}