package com.github.cao.awa.kora.status

import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class KoraStatusLocker {
    private val reloadingStatus: MutableMap<Any, Boolean> = mutableMapOf()
    private val queue: BlockingQueue<Boolean> = LinkedBlockingQueue()

    fun registerReloadable(reloadable: Any) {
        this.reloadingStatus[reloadable] = false
    }

    fun completedLifecycle(reloadable: Any) {
        this.reloadingStatus[reloadable] = true
        var done = false
        for ((_, status) in this.reloadingStatus) {
            if (!status) {
                break
            }
            done = true
        }
        if (done){
            this.queue.offer(true)
        }
    }

    @Throws(InterruptedException::class)
    fun await() {
         this.queue.take()
    }

    fun clear() {
        this.reloadingStatus.clear()
    }
}