package com.github.cao.awa.kora.server.network.http

import com.github.cao.awa.kora.constant.KoraInformation
import com.github.cao.awa.kora.server.network.group.KoraEventLoopGroupFactory
import com.github.cao.awa.kora.server.network.http.builder.KoraHttpServerBuilder
import com.github.cao.awa.kora.server.network.http.adapter.KoraHttpInboundHandlerAdapter
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpRequestDecoder
import io.netty.handler.codec.http.HttpResponseEncoder


class KoraHttpServer {
    companion object {
        var instructHttpMetadata: Boolean = true
        var instructHttpStatusCode: Boolean = true
        var instructHttpVersionCode: Boolean = true
    }

    private val serverBuilder: KoraHttpServerBuilder

    constructor(builder: KoraHttpServerBuilder) {
        this.serverBuilder = builder
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
                            // Only aggregate 2MB http request.
                            addLast(HttpObjectAggregator(KoraInformation.MB * 2))
                            addLast(KoraHttpInboundHandlerAdapter(this@KoraHttpServer.serverBuilder))
                        }
                    }
                })

            val future = bootstrap.bind(
                port
            ).sync()
            future.channel().closeFuture().sync()
        } finally {
            bossGroup.shutdownGracefully()
            workerGroup.shutdownGracefully()
        }
    }
}