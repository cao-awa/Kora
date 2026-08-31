package com.github.cao.awa.kalmia.server.network.http

import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class KalmiaHttpServerLocker {
    private val queue: BlockingQueue<Boolean> = LinkedBlockingQueue()

    fun onStop() {
        this.queue.offer(true)
    }

    @Throws(InterruptedException::class)
    fun await() {
         this.queue.take()
    }
}