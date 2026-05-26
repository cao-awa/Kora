package com.github.cao.awa.kora.server.network.http

import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class KoraHttpServerLocker {
    private val queue: BlockingQueue<Boolean> = LinkedBlockingQueue()

    fun onReloading() {
        this.queue.offer(true)
    }

    @Throws(InterruptedException::class)
    fun await() {
         this.queue.take()
    }
}