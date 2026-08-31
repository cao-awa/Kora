package com.github.cao.awa.kora.server.network.websocket.client

import com.github.cao.awa.kora.server.network.websocket.client.adapter.KoraWebSocketClientAdapter
import kotlin.reflect.KClass

class KoraWebSocketClient {
    private val handler: MutableMap<KClass<*>, (Any) -> Unit> = mutableMapOf()
    private lateinit var adapter: KoraWebSocketClientAdapter

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> registerHandler(clazz: KClass<T>, handler: (T) -> Unit) {
        this.handler[clazz] = {
            handler(it as T)
        }
    }

    fun setAdapter(adapter: KoraWebSocketClientAdapter) {
        this.adapter = adapter
    }

    fun fireMessage(msg: Any) {
        this.handler[msg::class]?.invoke(msg)
    }

    fun sendMessage(msg: Any) {
        this.adapter.sendMessage(msg)
    }
}