package com.courage.platform.rpc.remoting.common;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlatformSemaphoreReleaseOnlyOnce {

    private final AtomicBoolean released = new AtomicBoolean(false);

    private final Semaphore semaphore;

    public PlatformSemaphoreReleaseOnlyOnce(Semaphore semaphore) {
        this.semaphore = semaphore;
    }

    public void release() {
        if (this.semaphore != null) {
            if (this.released.compareAndSet(false, true)) {
                this.semaphore.release();
            }
        }
    }

    public Semaphore getSemaphore() {
        return semaphore;
    }
}
