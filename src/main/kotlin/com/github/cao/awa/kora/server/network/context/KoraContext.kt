package com.github.cao.awa.kora.server.network.context

import com.github.cao.awa.cason.obj.JSONObject
import com.github.cao.awa.cason.serialize.parser.StrictJSONParser
import com.github.cao.awa.kora.server.network.context.abort.KoraAbortContext
import com.github.cao.awa.kora.server.network.holder.PathByteBufHolder
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

abstract class KoraContext<B: PathByteBufHolder, C: KoraContext<B, C, A>, A: KoraAbortContext<B>>(private val msg: B) {
    fun content(): ByteArray {
        return this.msg.content().copy().let { content ->
            ByteArray(content.readableBytes()).also {
                content.readBytes(it)
            }
        }
    }

    fun stringContent(charset: Charset): String {
        return String(content(), charset)
    }

    fun stringContent(): String {
        return String(content(), StandardCharsets.UTF_8)
    }

    fun jsonContent(): JSONObject {
        return StrictJSONParser.parseObject(stringContent())
    }

    fun path(): String {
        return this.msg.path()
    }

    abstract fun createInherited(): C

    abstract fun createAbort(): A
}