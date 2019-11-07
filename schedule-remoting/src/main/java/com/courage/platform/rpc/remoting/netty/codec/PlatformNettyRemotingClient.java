package com.courage.platform.rpc.remoting.netty.codec;

import com.courage.platform.rpc.remoting.PlatformChannelEventListener;
import com.courage.platform.rpc.remoting.PlatformInvokeCallback;
import com.courage.platform.rpc.remoting.PlatformRemotingClient;
import com.courage.platform.rpc.remoting.PlatformRpcHook;
import com.courage.platform.rpc.remoting.common.PlatformPair;
import com.courage.platform.rpc.remoting.common.PlatformRemotingHelper;
import com.courage.platform.rpc.remoting.common.PlatformRemotingUtil;
import com.courage.platform.rpc.remoting.exception.PlatformRemotingConnectException;
import com.courage.platform.rpc.remoting.exception.PlatformRemotingSendRequestException;
import com.courage.platform.rpc.remoting.exception.PlatformRemotingTimeoutException;
import com.courage.platform.rpc.remoting.exception.PlatformRemotingTooMuchRequestException;
import com.courage.platform.rpc.remoting.netty.protocol.PlatformRemotingCommand;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PlatformNettyRemotingClient extends PlatformNettyRemotingAbstract implements PlatformRemotingClient {

    private static final Logger log = LoggerFactory.getLogger(PlatformRemotingHelper.PLATFORM_REMOTING);

    private final ConcurrentMap<String /* addr */, PlatformChannelWrapper> channelTables = new ConcurrentHashMap<String, PlatformChannelWrapper>();

    private final Timer timer = new Timer("PlatformClientHouseKeepingService", true);

    /**
     * Invoke the callback methods in this executor when process response.
     */
    private ExecutorService callbackExecutor;

    private static final long LOCK_TIMEOUT_MILLIS = 3000;

    private final PlatformChannelEventListener platformChannelEventListener;

    private final PlatformNettyClientConfig platformNettyClientConfig;

    private final ExecutorService publicExecutor;

    private final Bootstrap bootstrap = new Bootstrap();

    private final EventLoopGroup eventLoopGroupWorker;

    private DefaultEventExecutorGroup defaultEventExecutorGroup;

    private PlatformRpcHook rpcHook;

    private final Lock lockChannelTables = new ReentrantLock();

    public PlatformNettyRemotingClient(final PlatformNettyClientConfig platformNettyClientConfig) {
        this(platformNettyClientConfig, null);
    }

    public PlatformNettyRemotingClient(final PlatformNettyClientConfig platformNettyClientConfig,
                                       final PlatformChannelEventListener platformChannelEventListener) {
        super(platformNettyClientConfig.getClientOnewaySemaphoreValue(), platformNettyClientConfig.getClientAsyncSemaphoreValue());
        this.platformNettyClientConfig = platformNettyClientConfig;
        this.platformChannelEventListener = platformChannelEventListener;

        int publicThreadNums = platformNettyClientConfig.getClientCallbackExecutorThreads();
        if (publicThreadNums <= 0) {
            publicThreadNums = 4;
        }

        this.publicExecutor = Executors.newFixedThreadPool(publicThreadNums, new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "NettyClientPublicExecutor_" + this.threadIndex.incrementAndGet());
            }
        });

        this.eventLoopGroupWorker = new NioEventLoopGroup(1, new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, String.format("NettyClientSelector_%d", this.threadIndex.incrementAndGet()));
            }
        });
    }

    @Override
    public PlatformRemotingCommand invokeSync(String addr, PlatformRemotingCommand request, long timeoutMillis) throws InterruptedException, PlatformRemotingConnectException, PlatformRemotingSendRequestException, PlatformRemotingTimeoutException {
        final Channel channel = this.getAndCreateChannel(addr);
        if (channel != null && channel.isActive()) {
            try {
                if (this.rpcHook != null) {
                    this.rpcHook.doBeforeRequest(addr, request);
                }
                PlatformRemotingCommand response = this.invokeSyncImpl(channel, request, timeoutMillis);
                if (this.rpcHook != null) {
                    this.rpcHook.doAfterResponse(PlatformRemotingHelper.parseChannelRemoteAddr(channel), request, response);
                }
                return response;
            } catch (PlatformRemotingSendRequestException e) {
                log.warn("invokeSync: send request exception, so close the channel[{}]", addr);
                this.closeChannel(addr, channel);
                throw e;
            } catch (PlatformRemotingTimeoutException e) {
                if (platformNettyClientConfig.isClientCloseSocketIfTimeout()) {
                    this.closeChannel(addr, channel);
                    log.warn("invokeSync: close socket because of timeout, {}ms, {}", timeoutMillis, addr);
                }
                log.warn("invokeSync: wait response timeout exception, the channel[{}]", addr);
                throw e;
            }
        } else {
            this.closeChannel(addr, channel);
            throw new PlatformRemotingConnectException(addr);
        }
    }

    @Override
    public void invokeAsync(String addr, PlatformRemotingCommand request, long timeoutMillis, PlatformInvokeCallback invokeCallback)
            throws InterruptedException, PlatformRemotingConnectException, PlatformRemotingTooMuchRequestException, PlatformRemotingTimeoutException,
            PlatformRemotingSendRequestException {
        final Channel channel = this.getAndCreateChannel(addr);
        if (channel != null && channel.isActive()) {
            try {
                if (this.rpcHook != null) {
                    this.rpcHook.doBeforeRequest(addr, request);
                }
                this.invokeAsyncImpl(channel, request, timeoutMillis, invokeCallback);
            } catch (PlatformRemotingSendRequestException e) {
                log.warn("invokeAsync: send request exception, so close the channel[{}]", addr);
                this.closeChannel(addr, channel);
                throw e;
            }
        } else {
            this.closeChannel(addr, channel);
            throw new PlatformRemotingConnectException(addr);
        }
    }

    @Override
    public void invokeOneway(String addr, PlatformRemotingCommand request, long timeoutMillis) throws InterruptedException,
            PlatformRemotingConnectException, PlatformRemotingTooMuchRequestException, PlatformRemotingTimeoutException, PlatformRemotingSendRequestException {
        final Channel channel = this.getAndCreateChannel(addr);
        if (channel != null && channel.isActive()) {
            try {
                if (this.rpcHook != null) {
                    this.rpcHook.doBeforeRequest(addr, request);
                }
                this.invokeOnewayImpl(channel, request, timeoutMillis);
            } catch (PlatformRemotingSendRequestException e) {
                log.warn("invokeOneway: send request exception, so close the channel[{}]", addr);
                this.closeChannel(addr, channel);
                throw e;
            }
        } else {
            this.closeChannel(addr, channel);
            throw new PlatformRemotingConnectException(addr);
        }
    }


    @Override
    public void registerProcessor(int requestCmd, PlatformNettyRequestProcessor processor, ExecutorService executor) {
        ExecutorService executorThis = executor;
        if (null == executor) {
            executorThis = this.publicExecutor;
        }
        PlatformPair<PlatformNettyRequestProcessor, ExecutorService> pair = new PlatformPair<PlatformNettyRequestProcessor, ExecutorService>(processor, executorThis);
        this.processorTable.put(requestCmd, pair);
    }

    public void registerRpcHook(PlatformRpcHook rpcHook) {
        this.rpcHook = rpcHook;
    }

    @Override
    public void setCallbackExecutor(ExecutorService callbackExecutor) {
        this.callbackExecutor = callbackExecutor;
    }

    @Override
    public boolean isChannelWritable(String addr) {
        PlatformChannelWrapper cw = this.channelTables.get(addr);
        if (cw != null && cw.isOK()) {
            return cw.isWritable();
        }
        return true;
    }

    @Override
    public void start() {
        this.defaultEventExecutorGroup = new DefaultEventExecutorGroup(
                platformNettyClientConfig.getClientWorkerThreads(),
                new ThreadFactory() {

                    private AtomicInteger threadIndex = new AtomicInteger(0);

                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "PlatformNettyClientWorkerThread_" + this.threadIndex.incrementAndGet());
                    }
                });
        final PlatformNettyRemotingClient platformNettyRemotingClient = this;
        Bootstrap handler = this.bootstrap.group(this.eventLoopGroupWorker).channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, false)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, platformNettyClientConfig.getConnectTimeoutMillis())
                .option(ChannelOption.SO_SNDBUF, platformNettyClientConfig.getClientSocketSndBufSize())
                .option(ChannelOption.SO_RCVBUF, platformNettyClientConfig.getClientSocketRcvBufSize())
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(
                                defaultEventExecutorGroup,
                                new PlatformNettyEncoder(),
                                new PlatformNettyDecoder(),
                                new IdleStateHandler(0, 0, platformNettyClientConfig.getClientChannelMaxIdleTimeSeconds()),
                                new PlatformNettyClientConnectManageHandler(platformNettyRemotingClient),
                                new PlatformNettyClientHandler(platformNettyRemotingClient));
                    }
                });
        this.timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    PlatformNettyRemotingClient.this.scanResponseTable();
                } catch (Throwable e) {
                    log.error("scanResponseTable exception", e);
                }
            }
        }, 1000 * 3, 1000);
        if (this.platformChannelEventListener != null) {
            this.nettyEventExecutor.start();
        }
    }

    @Override
    public void shutdown() {
        try {
            this.timer.cancel();
            for (PlatformChannelWrapper cw : this.channelTables.values()) {
                this.closeChannel(null, cw.getChannel());
            }
            this.channelTables.clear();
            this.eventLoopGroupWorker.shutdownGracefully();
            if (this.nettyEventExecutor != null) {
                this.nettyEventExecutor.shutdown();
            }
            if (this.defaultEventExecutorGroup != null) {
                this.defaultEventExecutorGroup.shutdownGracefully();
            }
        } catch (Exception e) {
            log.error("PlatformNettyRemotingClient shutdown exception, ", e);
        }
        if (this.publicExecutor != null) {
            try {
                this.publicExecutor.shutdown();
            } catch (Exception e) {
                log.error("PlatformNettyRemotingServer shutdown exception, ", e);
            }
        }
    }

    public void closeChannel(final String addr, final Channel channel) {
        if (null == channel) {
            return;
        }
        final String addrRemote = null == addr ? PlatformRemotingHelper.parseChannelRemoteAddr(channel) : addr;
        try {
            if (this.lockChannelTables.tryLock(LOCK_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
                try {
                    boolean removeItemFromTable = true;
                    final PlatformChannelWrapper prevCW = this.channelTables.get(addrRemote);
                    log.info("closeChannel: begin close the channel[{}] Found: {}", addrRemote, prevCW != null);
                    if (null == prevCW) {
                        log.info("closeChannel: the channel[{}] has been removed from the channel table before", addrRemote);
                        removeItemFromTable = false;
                    } else if (prevCW.getChannel() != channel) {
                        log.info("closeChannel: the channel[{}] has been closed before, and has been created again, nothing to do.", addrRemote);
                        removeItemFromTable = false;
                    }
                    if (removeItemFromTable) {
                        this.channelTables.remove(addrRemote);
                        log.info("closeChannel: the channel[{}] was removed from channel table", addrRemote);
                    }
                    PlatformRemotingUtil.closeChannel(channel);
                } catch (Exception e) {
                    log.error("closeChannel: close the channel exception", e);
                } finally {
                    this.lockChannelTables.unlock();
                }
            } else {
                log.warn("closeChannel: try to lock channel table, but timeout, {}ms", LOCK_TIMEOUT_MILLIS);
            }
        } catch (InterruptedException e) {
            log.error("closeChannel exception", e);
        }
    }

    @Override
    public PlatformChannelEventListener getChannelEventListener() {
        return this.platformChannelEventListener;
    }

    @Override
    public ExecutorService getCallbackExecutor() {
        return callbackExecutor != null ? callbackExecutor : publicExecutor;
    }

    @Override
    public PlatformRpcHook getRpcHook() {
        return this.rpcHook;
    }

    static class PlatformChannelWrapper {

        private final ChannelFuture channelFuture;

        public PlatformChannelWrapper(ChannelFuture channelFuture) {
            this.channelFuture = channelFuture;
        }

        public boolean isOK() {
            return this.channelFuture.channel() != null && this.channelFuture.channel().isActive();
        }

        public boolean isWritable() {
            return this.channelFuture.channel().isWritable();
        }

        private Channel getChannel() {
            return this.channelFuture.channel();
        }

        public ChannelFuture getChannelFuture() {
            return channelFuture;
        }
    }

    private Channel getAndCreateChannel(final String addr) throws InterruptedException {
        PlatformChannelWrapper cw = this.channelTables.get(addr);
        if (cw != null && cw.isOK()) {
            return cw.getChannel();
        }
        return this.createChannel(addr);
    }

    public void closeChannel(final Channel channel) {
        if (null == channel)
            return;

        try {
            if (this.lockChannelTables.tryLock(LOCK_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
                try {
                    boolean removeItemFromTable = true;
                    PlatformChannelWrapper prevCW = null;
                    String addrRemote = null;
                    for (Map.Entry<String, PlatformChannelWrapper> entry : channelTables.entrySet()) {
                        String key = entry.getKey();
                        PlatformChannelWrapper prev = entry.getValue();
                        if (prev.getChannel() != null) {
                            if (prev.getChannel() == channel) {
                                prevCW = prev;
                                addrRemote = key;
                                break;
                            }
                        }
                    }

                    if (null == prevCW) {
                        log.info("eventCloseChannel: the channel[{}] has been removed from the channel table before", addrRemote);
                        removeItemFromTable = false;
                    }

                    if (removeItemFromTable) {
                        this.channelTables.remove(addrRemote);
                        log.info("closeChannel: the channel[{}] was removed from channel table", addrRemote);
                        PlatformRemotingUtil.closeChannel(channel);
                    }
                } catch (Exception e) {
                    log.error("closeChannel: close the channel exception", e);
                } finally {
                    this.lockChannelTables.unlock();
                }
            } else {
                log.warn("closeChannel: try to lock channel table, but timeout, {}ms", LOCK_TIMEOUT_MILLIS);
            }
        } catch (InterruptedException e) {
            log.error("closeChannel exception", e);
        }
    }

    private Channel createChannel(final String addr) throws InterruptedException {
        PlatformChannelWrapper cw = this.channelTables.get(addr);
        if (cw != null && cw.isOK()) {
            cw.getChannel().close();
            channelTables.remove(addr);
        }
        if (this.lockChannelTables.tryLock(LOCK_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
            try {
                boolean createNewConnection;
                cw = this.channelTables.get(addr);
                if (cw != null) {

                    if (cw.isOK()) {
                        cw.getChannel().close();
                        this.channelTables.remove(addr);
                        createNewConnection = true;
                    } else if (!cw.getChannelFuture().isDone()) {
                        createNewConnection = false;
                    } else {
                        this.channelTables.remove(addr);
                        createNewConnection = true;
                    }
                } else {
                    createNewConnection = true;
                }
                if (createNewConnection) {
                    ChannelFuture channelFuture = this.bootstrap.connect(PlatformRemotingHelper.string2SocketAddress(addr));
                    log.info("createChannel: begin to connect remote host[{}] asynchronously", addr);
                    cw = new PlatformChannelWrapper(channelFuture);
                    this.channelTables.put(addr, cw);
                }
            } catch (Exception e) {
                log.error("createChannel: create channel exception", e);
            } finally {
                this.lockChannelTables.unlock();
            }
        } else {
            log.warn("createChannel: try to lock channel table, but timeout, {}ms", LOCK_TIMEOUT_MILLIS);
        }

        if (cw != null) {
            ChannelFuture channelFuture = cw.getChannelFuture();
            if (channelFuture.awaitUninterruptibly(this.platformNettyClientConfig.getConnectTimeoutMillis())) {
                if (cw.isOK()) {
                    log.info("createChannel: connect remote host[{}] success, {}", addr, channelFuture.toString());
                    return cw.getChannel();
                } else {
                    log.warn("createChannel: connect remote host[" + addr + "] failed, " + channelFuture.toString(), channelFuture.cause());
                }
            } else {
                log.warn("createChannel: connect remote host[{}] timeout {}ms, {}", addr, this.platformNettyClientConfig.getConnectTimeoutMillis(), channelFuture.toString());
            }
        }
        return null;
    }

    public PlatformRemotingCommand invokeSyncImpl(final Channel channel, final PlatformRemotingCommand request, final long timeoutMillis) throws InterruptedException, PlatformRemotingSendRequestException, PlatformRemotingTimeoutException {
        final int opaque = request.getOpaque();
        try {
            final PlatformResponseFuture responseFuture = new PlatformResponseFuture(opaque, timeoutMillis, null, null);
            this.responseTable.put(opaque, responseFuture);
            final SocketAddress addr = channel.remoteAddress();
            channel.writeAndFlush(request).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture f) throws Exception {
                    if (f.isSuccess()) {
                        responseFuture.setSendRequestOK(true);
                        return;
                    } else {
                        responseFuture.setSendRequestOK(false);
                    }
                    responseTable.remove(opaque);
                    responseFuture.setCause(f.cause());
                    responseFuture.putResponse(null);
                    log.warn("send a request command to channel <" + addr + "> failed.");
                }
            });
            PlatformRemotingCommand responseCommand = responseFuture.waitResponse(timeoutMillis);
            if (null == responseCommand) {
                if (responseFuture.isSendRequestOK()) {
                    throw new PlatformRemotingTimeoutException(PlatformRemotingHelper.parseSocketAddressAddr(addr), timeoutMillis, responseFuture.getCause());
                } else {
                    throw new PlatformRemotingSendRequestException(PlatformRemotingHelper.parseSocketAddressAddr(addr), responseFuture.getCause());
                }
            }
            return responseCommand;
        } finally {
            this.responseTable.remove(opaque);
        }
    }

}
