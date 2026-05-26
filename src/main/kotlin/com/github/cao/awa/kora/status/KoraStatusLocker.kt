package com.github.cao.awa.kora.status

import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class KoraStatusLocker {
    private val lifecycleStatus: MutableList<Any> = mutableListOf()
    private val queue: BlockingQueue<Boolean> = LinkedBlockingQueue()

    fun registerLifecycle(lifecycleHolder: Any) {
        this.lifecycleStatus.add(lifecycleHolder)
    }

    fun completedLifecycle(lifecycleHolder: Any) {
        this.lifecycleStatus.remove(lifecycleHolder)
        if (this.lifecycleStatus.isEmpty()){
            this.queue.offer(true)
        }
    }

    @Throws(InterruptedException::class)
    fun await() {
         this.queue.take()
    }

    fun clear() {
        this.lifecycleStatus.clear()
    }
}