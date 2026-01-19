package com.github.cao.awa.kora.server.network.ws.protocol.handler

import com.github.cao.awa.kora.server.network.ws.config.KoraWebSocketServerProtocolConfig
import com.github.cao.awa.kora.server.network.ws.config.decoder.KoraWebSocketDecoderConfig
import com.github.cao.awa.kora.server.network.ws.holder.KoraTextWebsocketFrameHolder
import com.github.cao.awa.kora.server.network.ws.protocol.handler.handshake.KoraWebSocketServerProtocolHandshakeHandler
import io.netty.buffer.Unpooled
import io.netty.channel.*
import io.netty.handler.codec.MessageToMessageDecoder
import io.netty.handler.codec.http.DefaultFullHttpResponse
import io.netty.handler.codec.http.FullHttpResponse
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpVersion.HTTP_1_1
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import io.netty.handler.codec.http.websocketx.Utf8FrameValidator
import io.netty.handler.codec.http.websocketx.WebSocketCloseStatus
import io.netty.handler.codec.http.websocketx.WebSocketFrame
import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakeException
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler.HandshakeComplete
import io.netty.util.AttributeKey
import io.netty.util.ReferenceCountUtil
import io.netty.util.concurrent.Future
import io.netty.util.concurrent.PromiseNotifier
import java.net.SocketAddress
import java.nio.channels.ClosedChannelException
import java.util.concurrent.TimeUnit

class KoraWebSocketServerProtocolHandler(
    private val serverConfig: KoraWebSocketServerProtocolConfig
) : MessageToMessageDecoder<WebSocketFrame>(), ChannelOutboundHandler {
    companion object {
        const val DEFAULT_HANDSHAKE_TIMEOUT_MILLIS: Long = 10000L

        private val HANDSHAKER_ATTR_KEY: AttributeKey<WebSocketServerHandshaker> =
            AttributeKey.valueOf(WebSocketServerHandshaker::class.java, "HANDSHAKER")

        fun getHandshaker(channel: Channel): WebSocketServerHandshaker? {
            return channel.attr(HANDSHAKER_ATTR_KEY).get()
        }

        fun setHandshaker(channel: Channel, handshaker: WebSocketServerHandshaker) {
            channel.attr(HANDSHAKER_ATTR_KEY).set(handshaker)
        }
    }

    private val dropPongFrames: Boolean = this.serverConfig.dropPongFrames
    private val closeStatus: WebSocketCloseStatus = this.serverConfig.sendCloseFrame
    private val forceCloseTimeoutMillis: Long = this.serverConfig.forceCloseTimeoutMillis
    private var closeSent: ChannelPromise? = null
    private var uri: String? = null

    constructor(
        subprotocols: String?,
        forceCloseTimeoutMillis: Long,
        handleCloseFrames: Boolean,
        sendCloseFrame: WebSocketCloseStatus,
        checkStartsWith: Boolean,
        dropPongFrames: Boolean,
        decoderConfig: KoraWebSocketDecoderConfig,
        handshakeTimeoutMillis: Long = DEFAULT_HANDSHAKE_TIMEOUT_MILLIS
    ) : this(
        KoraWebSocketServerProtocolConfig(
            subprotocols,
            checkStartsWith,
            handshakeTimeoutMillis,
            forceCloseTimeoutMillis,
            handleCloseFrames,
            sendCloseFrame,
            dropPongFrames,
            decoderConfig
        )
    )

    override fun handlerAdded(ctx: ChannelHandlerContext) {
        val pipeline = ctx.pipeline()
        if (pipeline.get(KoraWebSocketServerProtocolHandshakeHandler::class.java.getName()) == null) {
            // Add the WebSocketHandshakeHandler before this one.
            pipeline.addBefore(
                ctx.name(),
                KoraWebSocketServerProtocolHandshakeHandler::class.java.getName(),
                KoraWebSocketServerProtocolHandshakeHandler(this.serverConfig)
            )
        }
        if (
            this.serverConfig.decoderConfig.withUTF8Validator &&
            pipeline.get(Utf8FrameValidator::class.java) == null
        ) {
            // Add the UFT8 checking before this one.
            pipeline.addBefore(
                ctx.name(),
                Utf8FrameValidator::class.java.getName(),
                Utf8FrameValidator(this.serverConfig.decoderConfig.closeOnProtocolViolation)
            )
        }
    }

    override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
        if (evt is HandshakeComplete) {
            this.uri = evt.requestUri()
        }
    }

    @Throws(Exception::class)
    protected override fun decode(ctx: ChannelHandlerContext, frame: WebSocketFrame, out: MutableList<Any>) {
        if (this.serverConfig.handleCloseFrames && frame is CloseWebSocketFrame) {
            val handshaker: WebSocketServerHandshaker? = getHandshaker(ctx.channel())
            if (handshaker != null) {
                frame.retain()
                val promise = ctx.newPromise()
                this.closeSent = promise
                handshaker.close(ctx, frame, promise)
            } else {
                ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE)
            }
            return
        }
        if (frame is PingWebSocketFrame) {
            frame.content().retain()
            ctx.writeAndFlush(PongWebSocketFrame(frame.content()))
            readIfNeeded(ctx)
            return
        }
        if (frame is PongWebSocketFrame && this.dropPongFrames) {
            readIfNeeded(ctx)
            return
        }

        if (frame is TextWebSocketFrame) {
            out.add(KoraTextWebsocketFrameHolder(frame.retain(), this.uri!!))
        }
    }


    @Throws(java.lang.Exception::class)
    override fun close(ctx: ChannelHandlerContext, promise: ChannelPromise?) {
        if (!ctx.channel().isActive) {
            ctx.close(promise)
        } else {
            if (this.closeSent == null) {
                write(ctx, CloseWebSocketFrame(this.closeStatus), ctx.newPromise())
            }
            flush(ctx)
            applyCloseSentTimeout(ctx)
            this.closeSent!!.addListener(object : ChannelFutureListener {
                override fun operationComplete(future: ChannelFuture?) {
                    ctx.close(promise)
                }
            })
        }
    }

    private fun applyCloseSentTimeout(ctx: ChannelHandlerContext) {
        if (this.closeSent!!.isDone || this.forceCloseTimeoutMillis < 0) {
            return
        }

        val timeoutTask: Future<*> = ctx.executor().schedule({
            if (!this.closeSent!!.isDone) {
                this.closeSent!!.tryFailure(buildHandshakeException("send close frame timed out"))
            }
        }, this.forceCloseTimeoutMillis, TimeUnit.MILLISECONDS)

        this.closeSent!!.addListener(object : ChannelFutureListener {
            override fun operationComplete(future: ChannelFuture?) {
                timeoutTask.cancel(false)
            }
        })
    }

    @Throws(java.lang.Exception::class)
    override fun write(ctx: ChannelHandlerContext, msg: Any?, promise: ChannelPromise) {
        if (this.closeSent != null) {
            ReferenceCountUtil.release(msg)
            promise.setFailure(ClosedChannelException())
        } else if (msg is CloseWebSocketFrame) {
            this.closeSent = promise.unvoid()
            ctx.write(msg).addListener(PromiseNotifier<Void?, ChannelFuture?>(false, this.closeSent))
        } else {
            ctx.write(msg, promise)
        }
    }

    private fun readIfNeeded(ctx: ChannelHandlerContext) {
        if (!ctx.channel().config().isAutoRead) {
            ctx.read()
        }
    }

    protected fun buildHandshakeException(message: String?): WebSocketServerHandshakeException {
        return WebSocketServerHandshakeException(message)
    }

    @Throws(java.lang.Exception::class)
    override fun bind(
        ctx: ChannelHandlerContext, localAddress: SocketAddress?,
        promise: ChannelPromise?
    ) {
        ctx.bind(localAddress, promise)
    }

    @Throws(java.lang.Exception::class)
    override fun connect(
        ctx: ChannelHandlerContext, remoteAddress: SocketAddress?,
        localAddress: SocketAddress?, promise: ChannelPromise?
    ) {
        ctx.connect(remoteAddress, localAddress, promise)
    }

    @Throws(java.lang.Exception::class)
    override fun disconnect(ctx: ChannelHandlerContext, promise: ChannelPromise?) {
        ctx.disconnect(promise)
    }

    @Throws(java.lang.Exception::class)
    override fun deregister(ctx: ChannelHandlerContext, promise: ChannelPromise?) {
        ctx.deregister(promise)
    }

    @Throws(java.lang.Exception::class)
    override fun read(ctx: ChannelHandlerContext) {
        ctx.read()
    }

    @Throws(java.lang.Exception::class)
    override fun flush(ctx: ChannelHandlerContext) {
        ctx.flush()
    }

    @Throws(Exception::class)
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        if (cause is WebSocketHandshakeException) {
            val response: FullHttpResponse = DefaultFullHttpResponse(
                HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.wrappedBuffer(cause.message!!.toByteArray())
            )
            ctx.channel().writeAndFlush(response).addListener(ChannelFutureListener.CLOSE)
        } else {
            ctx.fireExceptionCaught(cause)
            ctx.close()
        }
    }
}
