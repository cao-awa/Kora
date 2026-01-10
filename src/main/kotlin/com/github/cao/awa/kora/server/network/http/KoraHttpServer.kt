package com.github.cao.awa.kora.server.network.http

import com.github.cao.awa.kora.server.network.group.KoraEventLoopGroupFactory
import com.github.cao.awa.kora.server.network.http.builder.KoraHttpServerBuilder
import com.github.cao.awa.kora.server.network.http.handler.SimpleHttpServerHandler
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpRequestDecoder
import io.netty.handler.codec.http.HttpResponseEncoder


class KoraHttpServer {
    private val builder: KoraHttpServerBuilder.() -> Unit

    constructor(builder: KoraHttpServerBuilder.() -> Unit) {
        this.builder = builder
    }

    fun start(port: Int, useEpoll: Boolean = true) {
        val threadFactory = KoraEventLoopGroupFactory.remote(
            useEpoll
        )
        val bossGroup: EventLoopGroup = threadFactory.createEventLoopGroup()
        val workerGroup: EventLoopGroup = threadFactory.createEventLoopGroup()
        try {
            val bootstrap = ServerBootstrap()
                .group(
                    bossGroup,
                    workerGroup
                ).channel(
                    threadFactory.channel
                ).option(
                    ChannelOption.SO_BACKLOG, 256
                ).childOption(
                    ChannelOption.TCP_NODELAY, true
                ).childOption(
                    ChannelOption.SO_KEEPALIVE, true
                ).childOption(
                    ChannelOption.SO_RCVBUF, 65536
                ).childHandler(object : ChannelInitializer<SocketChannel>() {
                    @Override
                    override fun initChannel(channel: SocketChannel) {
                        channel.pipeline().apply {
                            addLast(HttpRequestDecoder())
                            addLast(HttpResponseEncoder())
                            // Only aggregate 1MB http request.
                            addLast(HttpObjectAggregator(1048576))
                            addLast(SimpleHttpServerHandler(this@KoraHttpServer.builder))
                        }
                    }
                })

            val future = bootstrap.bind(
                port
            ).sync()
            println("Server started on port $port")
            future.channel().closeFuture().sync()
        } finally {
            bossGroup.shutdownGracefully()
            workerGroup.shutdownGracefully()
        }
    }
}