package com.courage.platform.rpc.remoting.netty.codec;

import com.courage.platform.rpc.remoting.PlatformChannelEventListener;
import com.courage.platform.rpc.remoting.PlatformInvokeCallback;
import com.courage.platform.rpc.remoting.PlatformRemotingServer;
import com.courage.platform.rpc.remoting.PlatformRpcHook;
import com.courage.platform.rpc.remoting.common.PlatformPair;
import com.courage.platform.rpc.remoting.common.PlatformRemotingHelper;
import com.courage.platform.rpc.remoting.common.PlatformRemotingUtil;
import com.courage.platform.rpc.remoting.exception.PlatformRemotingSendRequestException;
import com.courage.platform.rpc.remoting.exception.PlatformRemotingTimeoutException;
import com.courage.platform.rpc.remoting.exception.PlatformRemotingTooMuchRequestException;
import com.courage.platform.rpc.remoting.netty.protocol.PlatformRemotingCommand;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class PlatformNettyRemotingServer extends PlatformNettyRemotingAbstract implements PlatformRemotingServer {

    private static final Logger log = LoggerFactory.getLogger(PlatformRemotingHelper.PLATFORM_REMOTING);

    private final ServerBootstrap serverBootstrap;

    private final EventLoopGroup eventLoopGroupSelector;

    private final EventLoopGroup eventLoopGroupBoss;

    private final PlatformNettyServerConfig platformNettyServerConfig;

    private final ExecutorService publicExecutor;

    private final PlatformChannelEventListener channelEventListener;

    private DefaultEventExecutorGroup defaultEventExecutorGroup;

    private PlatformRpcHook rpcHook;

    private final Timer timer = new Timer("PlatformServerHouseKeepingService", true);

    private int port = 0;

    public PlatformNettyRemotingServer(final PlatformNettyServerConfig nettyServerConfig) {
        this(nettyServerConfig, null);
    }

    public PlatformNettyRemotingServer(final PlatformNettyServerConfig platformNettyServerConfig,
                                       final PlatformChannelEventListener channelEventListener) {
        super(platformNettyServerConfig.getServerOnewaySemaphoreValue(), platformNettyServerConfig.getServerAsyncSemaphoreValue());
        this.serverBootstrap = new ServerBootstrap();
        this.platformNettyServerConfig = platformNettyServerConfig;
        this.channelEventListener = channelEventListener;

        int publicThreadNums = platformNettyServerConfig.getServerCallbackExecutorThreads();
        if (publicThreadNums <= 0) {
            publicThreadNums = 4;
        }
        this.publicExecutor = Executors.newFixedThreadPool(publicThreadNums, new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "PlatformNettyServerPublicExecutor_" + this.threadIndex.incrementAndGet());
            }
        });

        this.eventLoopGroupBoss = new NioEventLoopGroup(1, new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, String.format("PlatformNettyBoss_%d", this.threadIndex.incrementAndGet()));
            }
        });

        if (useEpoll()) {
            this.eventLoopGroupSelector = new EpollEventLoopGroup(platformNettyServerConfig.getServerSelectorThreads(), new ThreadFactory() {
                private AtomicInteger threadIndex = new AtomicInteger(0);
                private int threadTotal = platformNettyServerConfig.getServerSelectorThreads();

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("PlatformNettyServerEPOLLSelector_%d_%d", threadTotal, this.threadIndex.incrementAndGet()));
                }
            });
        } else {
            this.eventLoopGroupSelector = new NioEventLoopGroup(platformNettyServerConfig.getServerSelectorThreads(), new ThreadFactory() {
                private AtomicInteger threadIndex = new AtomicInteger(0);
                private int threadTotal = platformNettyServerConfig.getServerSelectorThreads();

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("PlatformNettyServerNIOSelector_%d_%d", threadTotal, this.threadIndex.incrementAndGet()));
                }
            });
        }
    }

    @Override
    public PlatformRpcHook getRpcHook() {
        return this.rpcHook;
    }

    private boolean useEpoll() {
        return PlatformRemotingUtil.isLinuxPlatform() && platformNettyServerConfig.isUseEpollNativeSelector() && Epoll.isAvailable();
    }

    @Override
    public void start() {
        final PlatformNettyRemotingAbstract platformNettyRemotingAbstract = this;
        this.defaultEventExecutorGroup = new DefaultEventExecutorGroup(
                platformNettyServerConfig.getServerWorkerThreads(),
                new ThreadFactory() {
                    private AtomicInteger threadIndex = new AtomicInteger(0);

                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "PlatformNettyServerCodecThread_" + this.threadIndex.incrementAndGet());
                    }
                });

        ServerBootstrap childHandler =
                this.serverBootstrap.group(this.eventLoopGroupBoss, this.eventLoopGroupSelector)
                        .channel(useEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                        .option(ChannelOption.SO_BACKLOG, 1024)
                        .option(ChannelOption.SO_REUSEADDR, true)
                        .option(ChannelOption.SO_KEEPALIVE, false)
                        .childOption(ChannelOption.TCP_NODELAY, true)
                        .childOption(ChannelOption.SO_SNDBUF, platformNettyServerConfig.getServerSocketSndBufSize())
                        .childOption(ChannelOption.SO_RCVBUF, platformNettyServerConfig.getServerSocketRcvBufSize())
                        .localAddress(new InetSocketAddress(this.platformNettyServerConfig.getListenPort()))
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(SocketChannel ch) throws Exception {
                                ch.pipeline().addLast(defaultEventExecutorGroup,
                                        new PlatformNettyEncoder(),
                                        new PlatformNettyDecoder(),
                                        new IdleStateHandler(0, 0, platformNettyServerConfig.getServerChannelMaxIdleTimeSeconds()),
                                        new PlatformNettyServerConnectManageHandler(platformNettyRemotingAbstract),
                                        new PlatformNettyServerHandler(platformNettyRemotingAbstract)
                                );
                            }
                        });
        if (platformNettyServerConfig.isServerPooledByteBufAllocatorEnable()) {
            childHandler.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        }
        try {
            ChannelFuture sync = this.serverBootstrap.bind().sync();
            InetSocketAddress addr = (InetSocketAddress) sync.channel().localAddress();
            this.port = addr.getPort();
            log.info("platformServer start port at {}", this.port);
        } catch (InterruptedException e1) {
            throw new RuntimeException("this.serverBootstrap.bind().sync() InterruptedException", e1);
        }
        if (this.channelEventListener != null) {
            this.nettyEventExecutor.start();
        }
        this.timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    PlatformNettyRemotingServer.this.scanResponseTable();
                } catch (Throwable e) {
                    log.error("scanResponseTable exception", e);
                }
            }
        }, 1000 * 3, 1000);
    }

    public void registerRpcHook(PlatformRpcHook rpcHook) {
        this.rpcHook = rpcHook;
    }

    @Override
    public void shutdown() {
        try {
            if (this.timer != null) {
                this.timer.cancel();
            }
            this.eventLoopGroupBoss.shutdownGracefully();
            this.eventLoopGroupSelector.shutdownGracefully();
            if (this.nettyEventExecutor != null) {
                this.nettyEventExecutor.shutdown();
            }
            if (this.defaultEventExecutorGroup != null) {
                this.defaultEventExecutorGroup.shutdownGracefully();
            }
        } catch (Exception e) {
            log.error("PlatformNettyRemotingServer shutdown exception, ", e);
        }
        if (this.publicExecutor != null) {
            try {
                this.publicExecutor.shutdown();
            } catch (Exception e) {
                log.error("PlatformNettyRemotingServer shutdown exception, ", e);
            }
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

    @Override
    public void registerDefaultProcessor(PlatformNettyRequestProcessor processor, ExecutorService executor) {
        this.defaultRequestProcessor = new PlatformPair<PlatformNettyRequestProcessor, ExecutorService>(processor, executor);
    }

    @Override
    public int localListenPort() {
        return this.port;
    }

    @Override
    public PlatformPair<PlatformNettyRequestProcessor, ExecutorService> getProcessorPair(int requestCode) {
        return processorTable.get(requestCode);
    }

    @Override
    public PlatformRemotingCommand invokeSync(Channel channel, PlatformRemotingCommand request, long timeoutMillis) throws InterruptedException, PlatformRemotingSendRequestException, PlatformRemotingTimeoutException {
        return this.invokeSyncImpl(channel, request, timeoutMillis);
    }

    @Override
    public void invokeAsync(Channel channel, PlatformRemotingCommand request, long timeoutMillis, PlatformInvokeCallback invokeCallback)
            throws InterruptedException, PlatformRemotingTooMuchRequestException, PlatformRemotingTimeoutException, PlatformRemotingSendRequestException {
        this.invokeAsyncImpl(channel, request, timeoutMillis, invokeCallback);
    }

    @Override
    public void invokeOneway(Channel channel, PlatformRemotingCommand request, long timeoutMillis) throws InterruptedException,
            PlatformRemotingTooMuchRequestException, PlatformRemotingTimeoutException, PlatformRemotingSendRequestException {
        this.invokeOnewayImpl(channel, request, timeoutMillis);
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

    @Override
    public PlatformChannelEventListener getChannelEventListener() {
        return this.channelEventListener;
    }

    @Override
    public ExecutorService getCallbackExecutor() {
        return this.publicExecutor;
    }

}
