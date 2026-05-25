package com.github.cao.awa.kora.server.network.http

import com.github.cao.awa.kora.KoraEntrypoint
import com.github.cao.awa.kora.launch.config.KoraLaunchConfig
import com.github.cao.awa.kora.launch.config.KoraLaunchDefaultConfig
import com.github.cao.awa.kora.constant.KoraInformation
import com.github.cao.awa.kora.server.network.group.KoraEventLoopGroupFactory
import com.github.cao.awa.kora.server.network.http.builder.KoraHttpServerBuilder
import com.github.cao.awa.kora.server.network.http.adapter.KoraHttpInboundHandlerAdapter
import com.github.cao.awa.kora.status.KoraStatus
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpRequestDecoder
import io.netty.handler.codec.http.HttpResponseEncoder
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.Scanner

class KoraHttpServer {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger("KoraHttpServer")
        var instructHttpMetadata: Boolean = true
        var instructHttpStatusCode: Boolean = true
        var instructHttpVersionCode: Boolean = true
        var instructRequestType: Boolean = true
        var instructRequestPath: Boolean = true
    }

    private val serverBuilder: KoraHttpServerBuilder
    private var running = false
    val isRunning: Boolean
        get() = this.running

    constructor(builder: KoraHttpServerBuilder) {
        this.serverBuilder = builder
    }

    fun start(
        port: Int,
        address: String = "localhost",
        io: KoraEventLoopGroupFactory = KoraEventLoopGroupFactory.remote(),
        launchConfig: KoraLaunchConfig = KoraLaunchDefaultConfig
    ) {
        val nettyConfig = launchConfig.nettyServerConfig()
        val threadFactory = KoraEventLoopGroupFactory.validate(io)
        val bossGroup: EventLoopGroup = threadFactory.createEventLoopGroup(2)
        val workerGroup: EventLoopGroup = threadFactory.createEventLoopGroup(
            Runtime.getRuntime().availableProcessors() * 2
        )
        val adapter = KoraHttpInboundHandlerAdapter(this.serverBuilder, launchConfig)
        try {
            val bootstrap = ServerBootstrap()
                .group(
                    bossGroup,
                    workerGroup
                ).channel(
                    threadFactory.channel
                ).option(
                    ChannelOption.SO_BACKLOG, nettyConfig.backlog()
                ).childOption(
                    ChannelOption.TCP_NODELAY, nettyConfig.tcpNoDelay()
                ).childOption(
                    ChannelOption.SO_KEEPALIVE, nettyConfig.keepalive()
                ).childOption(
                    ChannelOption.SO_RCVBUF, nettyConfig.rcvBuf()
                ).childOption(
                    ChannelOption.SO_REUSEADDR, nettyConfig.reuseAddr()
                ).childOption(
                    ChannelOption.WRITE_BUFFER_WATER_MARK, nettyConfig.writeBufferWaterMark()
                ).childOption(
                    ChannelOption.ALLOCATOR, nettyConfig.allocator()
                ).childHandler(object : ChannelInitializer<SocketChannel>() {
                    @Override
                    override fun initChannel(channel: SocketChannel) {
                        channel.pipeline().apply {
                            addLast( HttpRequestDecoder())
                            addLast(HttpResponseEncoder())
                            // Only aggregate 1MB http request.
                            addLast( HttpObjectAggregator(KoraInformation.MB))
                            addLast(adapter)
                        }
                    }
                })

            val future = bootstrap.bind(
                address,
                port
            ).sync()
            this.running = true
            LOGGER.info("Kora HTTP server started on port {} on {}", port, address)
            future.channel().closeFuture().addListener {
                LOGGER.info("Stopping Kora HTTP server")
            }

            val scanner = Scanner(System.`in`)
            while (this.running) {
                when (val input = scanner.nextLine()) {
                    "stop", "exit" -> {
                        this.running = false
                        KoraStatus.stop()
                    }
                    "reload" -> {
                        this.running = false
                        KoraStatus.reloading = true
                    }
                    else -> LOGGER.info("Unknown command: $input")
                }
            }
        } finally {
            bossGroup.shutdownGracefully().sync()
            workerGroup.shutdownGracefully().sync()
        }

        LOGGER.info("Kora HTTP server stopped")
    }
}