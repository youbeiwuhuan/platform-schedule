package com.courage.platform.rpc.remoting.netty.codec;

import com.courage.platform.rpc.remoting.PlatformChannelEventListener;
import com.courage.platform.rpc.remoting.PlatformInvokeCallback;
import com.courage.platform.rpc.remoting.PlatformRpcHook;
import com.courage.platform.rpc.remoting.common.PlatformPair;
import com.courage.platform.rpc.remoting.common.PlatformRemotingHelper;
import com.courage.platform.rpc.remoting.common.PlatformSemaphoreReleaseOnlyOnce;
import com.courage.platform.rpc.remoting.common.PlatformServiceThread;
import com.courage.platform.rpc.remoting.exception.PlatformRemotingSendRequestException;
import com.courage.platform.rpc.remoting.exception.PlatformRemotingTimeoutException;
import com.courage.platform.rpc.remoting.exception.PlatformRemotingTooMuchRequestException;
import com.courage.platform.rpc.remoting.netty.protocol.PlatformRemotingCommand;
import com.courage.platform.rpc.remoting.netty.protocol.PlatformRemotingCommandFormat;
import com.courage.platform.rpc.remoting.netty.protocol.PlatformRemotingCommandUtils;
import com.courage.platform.rpc.remoting.netty.protocol.PlatformRemotingSysResponseCode;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

public abstract class PlatformNettyRemotingAbstract {

    private final static Logger log = LoggerFactory.getLogger(PlatformRemotingHelper.PLATFORM_REMOTING);

    protected final ConcurrentMap<Integer /* opaque */, PlatformResponseFuture> responseTable =
            new ConcurrentHashMap<Integer, PlatformResponseFuture>(256);

    protected final HashMap<Integer/* request code */, PlatformPair<PlatformNettyRequestProcessor, ExecutorService>> processorTable =
            new HashMap<Integer, PlatformPair<PlatformNettyRequestProcessor, ExecutorService>>(64);

    /**
     * Semaphore to limit maximum number of on-going one-way requests, which protects system memory footprint.
     */
    protected final Semaphore semaphoreOneway;

    /**
     * Semaphore to limit maximum number of on-going asynchronous requests, which protects system memory footprint.
     */
    protected final Semaphore semaphoreAsync;


    /**
     * Constructor, specifying capacity of one-way and asynchronous semaphores.
     *
     * @param permitsOneway Number of permits for one-way requests.
     * @param permitsAsync  Number of permits for asynchronous requests.
     */
    public PlatformNettyRemotingAbstract(final int permitsOneway, final int permitsAsync) {
        this.semaphoreOneway = new Semaphore(permitsOneway, true);
        this.semaphoreAsync = new Semaphore(permitsAsync, true);
    }


    /**
     * The default request processor to use in case there is no exact match in {@link #processorTable} per request code.
     */
    protected PlatformPair<PlatformNettyRequestProcessor, ExecutorService> defaultRequestProcessor;

    public void scanResponseTable() {
        final List<PlatformResponseFuture> rfList = new LinkedList<PlatformResponseFuture>();
        Iterator<Map.Entry<Integer, PlatformResponseFuture>> it = this.responseTable.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, PlatformResponseFuture> next = it.next();
            PlatformResponseFuture rep = next.getValue();
            if ((rep.getBeginTimestamp() + rep.getTimeoutMillis() + 1000) <= System.currentTimeMillis()) {
                rep.release();
                it.remove();
                rfList.add(rep);
                log.warn("remove timeout request, " + rep);
            }
        }
        for (PlatformResponseFuture rf : rfList) {
            try {
                executeInvokeCallback(rf);
            } catch (Throwable e) {
                log.warn("scanResponseTable, operationComplete Exception", e);
            }
        }
    }

    private void executeInvokeCallback(final PlatformResponseFuture responseFuture) {
        boolean runInThisThread = false;
        ExecutorService executor = this.getCallbackExecutor();
        if (executor != null) {
            try {
                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            responseFuture.executeInvokeCallback();
                        } catch (Throwable e) {
                            log.warn("execute callback in executor exception, and callback throw", e);
                        } finally {
                            responseFuture.release();
                        }
                    }
                });
            } catch (Exception e) {
                runInThisThread = true;
                log.warn("execute callback in executor exception, maybe executor busy", e);
            }
        } else {
            runInThisThread = true;
        }
        if (runInThisThread) {
            try {
                responseFuture.executeInvokeCallback();
            } catch (Throwable e) {
                log.warn("executeInvokeCallback Exception", e);
            } finally {
                responseFuture.release();
            }
        }
    }

    protected final PlatformNettyEventExecutor nettyEventExecutor = new PlatformNettyEventExecutor();

    class PlatformNettyEventExecutor extends PlatformServiceThread {
        private final LinkedBlockingQueue<PlatformNettyEvent> eventQueue = new LinkedBlockingQueue<PlatformNettyEvent>();
        private final int maxSize = 10000;

        public void putNettyEvent(final PlatformNettyEvent event) {
            if (this.eventQueue.size() <= maxSize) {
                this.eventQueue.add(event);
            } else {
                log.warn("event queue size[{}] enough, so drop this event {}", this.eventQueue.size(), event.toString());
            }
        }

        @Override
        public void run() {
            log.info(this.getServiceName() + " service started");
            final PlatformChannelEventListener listener = PlatformNettyRemotingAbstract.this.getChannelEventListener();
            while (!this.isStopped()) {
                try {
                    PlatformNettyEvent event = this.eventQueue.poll(3000, TimeUnit.MILLISECONDS);
                    if (event != null && listener != null) {
                        switch (event.getType()) {
                            case IDLE:
                                listener.onChannelIdle(event.getRemoteAddr(), event.getChannel());
                                break;
                            case CLOSE:
                                listener.onChannelClose(event.getRemoteAddr(), event.getChannel());
                                break;
                            case CONNECT:
                                listener.onChannelConnect(event.getRemoteAddr(), event.getChannel());
                                break;
                            case EXCEPTION:
                                listener.onChannelException(event.getRemoteAddr(), event.getChannel());
                                break;
                            default:
                                break;
                        }
                    }
                } catch (Exception e) {
                    log.warn(this.getServiceName() + " service has exception. ", e);
                }
            }
            log.info(this.getServiceName() + " service end");
        }

        @Override
        public String getServiceName() {
            return PlatformNettyEventExecutor.class.getSimpleName();
        }
    }

    /**
     * Process incoming request command issued by remote peer.
     *
     * @param ctx channel handler context.
     * @param cmd request command.
     */
    public void processRequestCommand(final ChannelHandlerContext ctx, final PlatformRemotingCommand cmd) {
        final PlatformPair<PlatformNettyRequestProcessor, ExecutorService> matched = this.processorTable.get(cmd.getRequestCmd());
        final PlatformPair<PlatformNettyRequestProcessor, ExecutorService> pair = null == matched ? this.defaultRequestProcessor : matched;
        final int opaque = cmd.getOpaque();

        if (pair != null) {
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    try {
                        PlatformRpcHook rpcHook = PlatformNettyRemotingAbstract.this.getRpcHook();
                        if (rpcHook != null) {
                            rpcHook.doBeforeRequest(PlatformRemotingHelper.parseChannelRemoteAddr(ctx.channel()), cmd);
                        }
                        final PlatformRemotingCommand response = pair.getObject1().processRequest(ctx, cmd);
                        if (rpcHook != null) {
                            rpcHook.doAfterResponse(PlatformRemotingHelper.parseChannelRemoteAddr(ctx.channel()), cmd, response);
                        }
                        if (cmd.getFormat() != PlatformRemotingCommandFormat.REQUESET_ONEWAY.getCode()) {
                            if (response != null) {
                                response.setRequestCmd(cmd.getRequestCmd());
                                response.setOpaque(opaque);
                                response.setFormat(PlatformRemotingCommandFormat.RESPONSE.getCode());
                                try {
                                    ctx.writeAndFlush(response);
                                } catch (Throwable e) {
                                    log.error("process request over, but response failed", e);
                                    log.error(cmd.toString());
                                    log.error(response.toString());
                                }
                            } else {
                            }
                        }
                    } catch (Throwable e) {
                        log.error("process request exception", e);
                        log.error(cmd.toString());
                        if (cmd.getFormat() != PlatformRemotingCommandFormat.REQUESET_ONEWAY.getCode()) {
                            final PlatformRemotingCommand response = PlatformRemotingCommandUtils.createResponseCommand(PlatformRemotingSysResponseCode.SYSTEM_ERROR,
                                    PlatformRemotingHelper.exceptionSimpleDesc(e));
                            response.setOpaque(opaque);
                            ctx.writeAndFlush(response);
                        }
                    }
                }
            };
            if (pair.getObject1().rejectRequest()) {
                final PlatformRemotingCommand response = PlatformRemotingCommandUtils.createResponseCommand(PlatformRemotingSysResponseCode.SYSTEM_BUSY,
                        "[REJECTREQUEST]system busy, start flow control for a while");
                response.setOpaque(opaque);
                ctx.writeAndFlush(response);
                return;
            }
            try {
                final PlatformRequestTask requestTask = new PlatformRequestTask(run, ctx.channel(), cmd);
                pair.getObject2().submit(requestTask);
            } catch (RejectedExecutionException e) {
                if ((System.currentTimeMillis() % 10000) == 0) {
                    log.warn(PlatformRemotingHelper.parseChannelRemoteAddr(ctx.channel())
                            + ", too many requests and system thread pool busy, RejectedExecutionException "
                            + pair.getObject2().toString()
                            + " request code: " + cmd.getCode());
                }
                if (cmd.getFormat() != PlatformRemotingCommandFormat.REQUESET_ONEWAY.getCode()) {
                    final PlatformRemotingCommand response = PlatformRemotingCommandUtils.createResponseCommand(PlatformRemotingSysResponseCode.SYSTEM_BUSY,
                            "[OVERLOAD]system busy, start flow control for a while");
                    response.setOpaque(opaque);
                    ctx.writeAndFlush(response);
                }
            }
        } else {
            String error = " requestcmd type " + cmd.getRequestCmd() + " not supported";
            final PlatformRemotingCommand response = PlatformRemotingCommandUtils.createResponseCommand(PlatformRemotingSysResponseCode.REQUEST_CODE_NOT_SUPPORTED,
                    error);
            response.setOpaque(opaque);
            ctx.writeAndFlush(response);
            log.error(PlatformRemotingHelper.parseChannelRemoteAddr(ctx.channel()) + error);
        }
    }

    public void processMessageReceived(ChannelHandlerContext ctx, PlatformRemotingCommand msg) throws Exception {
        final PlatformRemotingCommand cmd = msg;
        if (cmd != null) {
            if (cmd.getFormat() == PlatformRemotingCommandFormat.REQUESET.getCode() || cmd.getFormat() == PlatformRemotingCommandFormat.REQUESET_ONEWAY.getCode()) {
                processRequestCommand(ctx, cmd);
            } else if (cmd.getFormat() == PlatformRemotingCommandFormat.RESPONSE.getCode()) {
                processResponseCommand(ctx, cmd);
            } else {
                log.error("unsupport commandformat: " + msg);
            }
        }
    }

    /**
     * Process response from remote peer to the previous issued requests.
     *
     * @param ctx channel handler context.
     * @param cmd response command instance.
     */
    public void processResponseCommand(ChannelHandlerContext ctx, PlatformRemotingCommand cmd) {
        final int opaque = cmd.getOpaque();
        final PlatformResponseFuture responseFuture = responseTable.get(opaque);
        if (responseFuture != null) {
            responseFuture.setResponseCommand(cmd);
            responseTable.remove(opaque);
            if (responseFuture.getInvokeCallback() != null) {
                executeInvokeCallback(responseFuture);
            } else {
                responseFuture.putResponse(cmd);
                responseFuture.release();
            }
        } else {
            log.warn("receive response, but not matched any request, " + PlatformRemotingHelper.parseChannelRemoteAddr(ctx.channel()));
            log.warn(cmd.toString());
        }
    }

    public void invokeAsyncImpl(final Channel channel, final PlatformRemotingCommand request, final long timeoutMillis, final PlatformInvokeCallback invokeCallback)
            throws InterruptedException, PlatformRemotingTooMuchRequestException, PlatformRemotingTimeoutException, PlatformRemotingSendRequestException {
        final int opaque = request.getOpaque();
        boolean acquired = this.semaphoreAsync.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS);
        if (acquired) {
            final PlatformSemaphoreReleaseOnlyOnce once = new PlatformSemaphoreReleaseOnlyOnce(this.semaphoreAsync);

            final PlatformResponseFuture responseFuture = new PlatformResponseFuture(opaque, timeoutMillis, invokeCallback, once);
            this.responseTable.put(opaque, responseFuture);
            try {
                channel.writeAndFlush(request).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture f) throws Exception {
                        if (f.isSuccess()) {
                            responseFuture.setSendRequestOK(true);
                            return;
                        } else {
                            responseFuture.setSendRequestOK(false);
                        }

                        responseFuture.putResponse(null);
                        responseTable.remove(opaque);
                        try {
                            executeInvokeCallback(responseFuture);
                        } catch (Throwable e) {
                            log.warn("excute callback in writeAndFlush addListener, and callback throw", e);
                        } finally {
                            responseFuture.release();
                        }

                        log.warn("send a request command to channel <{}> failed.", PlatformRemotingHelper.parseChannelRemoteAddr(channel));
                    }
                });
            } catch (Exception e) {
                responseFuture.release();
                log.warn("send a request command to channel <" + PlatformRemotingHelper.parseChannelRemoteAddr(channel) + "> Exception", e);
                throw new PlatformRemotingSendRequestException(PlatformRemotingHelper.parseChannelRemoteAddr(channel), e);
            }
        } else {
            if (timeoutMillis <= 0) {
                throw new PlatformRemotingTooMuchRequestException("invokeAsyncImpl invoke too fast");
            } else {
                String info =
                        String.format("invokeAsyncImpl tryAcquire semaphore timeout, %dms, waiting thread nums: %d semaphoreAsyncValue: %d",
                                timeoutMillis,
                                this.semaphoreAsync.getQueueLength(),
                                this.semaphoreAsync.availablePermits()
                        );
                log.warn(info);
                throw new PlatformRemotingTimeoutException(info);
            }
        }
    }

    public void invokeOnewayImpl(final Channel channel, final PlatformRemotingCommand request, final long timeoutMillis)
            throws InterruptedException, PlatformRemotingTooMuchRequestException, PlatformRemotingTimeoutException, PlatformRemotingSendRequestException {
        request.setFormat(PlatformRemotingCommandFormat.REQUESET_ONEWAY.getCode());
        boolean acquired = this.semaphoreOneway.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS);
        if (acquired) {
            final PlatformSemaphoreReleaseOnlyOnce once = new PlatformSemaphoreReleaseOnlyOnce(this.semaphoreOneway);
            try {
                channel.writeAndFlush(request).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture f) throws Exception {
                        once.release();
                        if (!f.isSuccess()) {
                            log.warn("send a request command to channel <" + channel.remoteAddress() + "> failed.");
                        }
                    }
                });
            } catch (Exception e) {
                once.release();
                log.warn("write send a request command to channel <" + channel.remoteAddress() + "> failed.");
                throw new PlatformRemotingSendRequestException(PlatformRemotingHelper.parseChannelRemoteAddr(channel), e);
            }
        } else {
            if (timeoutMillis <= 0) {
                throw new PlatformRemotingTooMuchRequestException("invokeOnewayImpl invoke too fast");
            } else {
                String info = String.format(
                        "invokeOnewayImpl tryAcquire semaphore timeout, %dms, waiting thread nums: %d semaphoreAsyncValue: %d",
                        timeoutMillis,
                        this.semaphoreOneway.getQueueLength(),
                        this.semaphoreOneway.availablePermits()
                );
                log.warn(info);
                throw new PlatformRemotingTimeoutException(info);
            }
        }
    }


    public abstract PlatformChannelEventListener getChannelEventListener();

    public abstract ExecutorService getCallbackExecutor();

    public abstract PlatformRpcHook getRpcHook();

}
