package com.github.cao.awa.kora.server.network.http.handler

import com.github.cao.awa.kora.server.KoraInformation
import com.github.cao.awa.kora.server.network.http.error.KoraHttpError
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.http.*
import io.netty.util.CharsetUtil


class SimpleHttpServerHandler : ChannelInboundHandlerAdapter() {
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        when (msg) {
            is FullHttpRequest -> {
                val response: FullHttpResponse = DefaultFullHttpResponse(
                    msg.protocolVersion(),
                    HttpResponseStatus.OK,
                    Unpooled.copiedBuffer("Hello Kora!", CharsetUtil.UTF_8)
                )

                response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain")
                response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes())

                if (HttpUtil.isKeepAlive(msg)) {
                    response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)
                }

                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE)
            }
            is HttpRequest -> {
                ctx.writeAndFlush(
                    KoraHttpError.FAILURE_NOT_FULL(msg.protocolVersion())
                )
            }
            else -> {
                ctx.writeAndFlush(
                    KoraHttpError.INTERNAL_SERVER_ERROR(HttpVersion.HTTP_1_0)
                )
            }
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        ctx.close()
    }
}
