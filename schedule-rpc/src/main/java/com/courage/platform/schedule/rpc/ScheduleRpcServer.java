package com.courage.platform.schedule.rpc;

import com.courage.platform.rpc.remoting.PlatformChannelEventListener;
import com.courage.platform.rpc.remoting.netty.codec.NodePlatformRemotingServer;
import com.courage.platform.rpc.remoting.netty.codec.PlatformNettyRequestProcessor;
import com.courage.platform.rpc.remoting.netty.codec.PlatformNettyServerConfig;
import com.courage.platform.schedule.rpc.config.ScheduleRpcServerConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.BindException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 任务服务端
 * Created by zhangyong on 2018/10/3.
 */
public class ScheduleRpcServer implements ScheduleRpcService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleRpcServer.class);

    private NodePlatformRemotingServer nodePlatformRemotingServer;

    private PlatformNettyServerConfig platformNettyServerConfig;

    //默认12999
    private int listenPort = ScheduleRpcServerConfig.TASK_PRC_LISTEN_PORT;

    private final static int POOL_CORE_SIZE = 20;

    private final static ThreadPoolExecutor scheduleRpcThreadPool = new ThreadPoolExecutor(POOL_CORE_SIZE, POOL_CORE_SIZE, 500, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(100000), new ThreadFactory() {
        private AtomicInteger threadIndex = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "ScheduleRpcThreadPool_" + this.threadIndex.incrementAndGet());
        }
    }, new ThreadPoolExecutor.CallerRunsPolicy());

    private Map<Integer, PlatformNettyRequestProcessor> processorTable = new HashMap<Integer, PlatformNettyRequestProcessor>(4);

    private PlatformChannelEventListener platformChannelEventListener;

    public void start() {
        this.platformNettyServerConfig = new PlatformNettyServerConfig();
        platformNettyServerConfig.setListenPort(ScheduleRpcServerConfig.TASK_PRC_LISTEN_PORT);
        platformNettyServerConfig.setServerChannelMaxIdleTimeSeconds(ScheduleRpcServerConfig.MAX_IDLE_TIME);
        this.nodePlatformRemotingServer = new NodePlatformRemotingServer(platformNettyServerConfig, platformChannelEventListener) {
            @Override
            public void boot(ServerBootstrap serverBootstrap) {
                try {
                    boolean bindSuccess = false;
                    while (!bindSuccess) {
                        try {
                            ChannelFuture sync = serverBootstrap.bind(listenPort);
                            sync.sync();
                            bindSuccess = true;
                            logger.info("启动Node服务，监听端口:" + listenPort);
                            InetSocketAddress addr = (InetSocketAddress) sync.channel().localAddress();
                        } catch (Exception e) {
                            if (e instanceof BindException) {
                                logger.warn(e.getMessage() + ", port=" + listenPort + ", we will try a new port");
                                listenPort = listenPort + 1;
                                if (listenPort > 65535) {
                                    listenPort = 40000;
                                }
                            } else if (e instanceof InterruptedException) {
                                throw (InterruptedException) e;
                            } else {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                } catch (InterruptedException e1) {
                    throw new RuntimeException("this.serverBootstrap.bind().sync() InterruptedException", e1);
                }
            }
        };
        synchronized (this) {
            //添加命令处理器
            Set<Integer> set = processorTable.keySet();
            for (Integer cmd : set) {
                this.nodePlatformRemotingServer.registerProcessor(cmd, processorTable.get(cmd), scheduleRpcThreadPool);
            }
        }
        this.nodePlatformRemotingServer.start();
    }

    public void shutdown() {
        this.nodePlatformRemotingServer.shutdown();
        this.scheduleRpcThreadPool.shutdown();
    }

    public Map<Integer, PlatformNettyRequestProcessor> getProcessorTable() {
        return processorTable;
    }

    public void setProcessorTable(Map<Integer, PlatformNettyRequestProcessor> processorTable) {
        this.processorTable = processorTable;
    }

    public int localListenPort() {
        return this.listenPort;
    }

    public PlatformChannelEventListener getPlatformChannelEventListener() {
        return platformChannelEventListener;
    }

    public void setPlatformChannelEventListener(PlatformChannelEventListener platformChannelEventListener) {
        this.platformChannelEventListener = platformChannelEventListener;
    }

    public NodePlatformRemotingServer getNodePlatformRemotingServer() {
        return nodePlatformRemotingServer;
    }
}
