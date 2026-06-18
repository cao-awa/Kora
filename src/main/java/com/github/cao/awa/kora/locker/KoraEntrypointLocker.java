package com.github.cao.awa.kora.locker;

import java.util.concurrent.LinkedBlockingQueue;

public class KoraEntrypointLocker {
    private final LinkedBlockingQueue<Boolean> QUEUE = new LinkedBlockingQueue<>();

    public boolean waitFor() {
        return Boolean.TRUE.equals(QUEUE.poll());
    }

    public void offer() {
        QUEUE.offer(true);
    }
}
