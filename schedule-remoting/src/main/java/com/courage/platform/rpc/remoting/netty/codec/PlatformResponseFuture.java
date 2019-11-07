package com.courage.platform.rpc.remoting.netty.codec;

import com.courage.platform.rpc.remoting.PlatformInvokeCallback;
import com.courage.platform.rpc.remoting.common.PlatformSemaphoreReleaseOnlyOnce;
import com.courage.platform.rpc.remoting.netty.protocol.PlatformRemotingCommand;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlatformResponseFuture {

    private final int opaque;

    private final long timeoutMillis;

    private final PlatformInvokeCallback invokeCallback;

    private final long beginTimestamp = System.currentTimeMillis();

    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    private final PlatformSemaphoreReleaseOnlyOnce once;

    private final AtomicBoolean executeCallbackOnlyOnce = new AtomicBoolean(false);

    private volatile PlatformRemotingCommand responseCommand;

    private volatile boolean sendRequestOK = true;

    private volatile Throwable cause;

    public PlatformResponseFuture(int opaque, long timeoutMillis, PlatformInvokeCallback invokeCallback,
                                  PlatformSemaphoreReleaseOnlyOnce once) {
        this.opaque = opaque;
        this.timeoutMillis = timeoutMillis;
        this.invokeCallback = invokeCallback;
        this.once = once;
    }

    public void executeInvokeCallback() {
        if (invokeCallback != null) {
            if (this.executeCallbackOnlyOnce.compareAndSet(false, true)) {
                invokeCallback.operationComplete(this);
            }
        }
    }

    public void release() {
        if (this.once != null) {
            this.once.release();
        }
    }

    public boolean isTimeout() {
        long diff = System.currentTimeMillis() - this.beginTimestamp;
        return diff > this.timeoutMillis;
    }

    public PlatformRemotingCommand waitResponse(final long timeoutMillis) throws InterruptedException {
        this.countDownLatch.await(timeoutMillis, TimeUnit.MILLISECONDS);
        return this.responseCommand;
    }

    public void putResponse(final PlatformRemotingCommand responseCommand) {
        this.responseCommand = responseCommand;
        this.countDownLatch.countDown();
    }

    public long getBeginTimestamp() {
        return beginTimestamp;
    }

    public boolean isSendRequestOK() {
        return sendRequestOK;
    }

    public void setSendRequestOK(boolean sendRequestOK) {
        this.sendRequestOK = sendRequestOK;
    }

    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    public PlatformInvokeCallback getInvokeCallback() {
        return invokeCallback;
    }

    public Throwable getCause() {
        return cause;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    public PlatformRemotingCommand getResponseCommand() {
        return responseCommand;
    }

    public void setResponseCommand(PlatformRemotingCommand responseCommand) {
        this.responseCommand = responseCommand;
    }

    public int getOpaque() {
        return opaque;
    }

    @Override
    public String toString() {
        return "ResponseFuture [responseCommand=" + responseCommand + ", sendRequestOK=" + sendRequestOK
            + ", cause=" + cause + ", opaque=" + opaque + ", timeoutMillis=" + timeoutMillis
            + ", invokeCallback=" + invokeCallback + ", beginTimestamp=" + beginTimestamp
            + ", countDownLatch=" + countDownLatch + "]";
    }
}
