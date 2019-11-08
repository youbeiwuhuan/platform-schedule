package com.courage.platform.schedule.rpc;

import com.courage.platform.rpc.remoting.netty.codec.PlatformNettyClientConfig;
import com.courage.platform.rpc.remoting.netty.codec.PlatformNettyRemotingClient;
import com.courage.platform.rpc.remoting.netty.codec.PlatformNettyRequestProcessor;
import com.courage.platform.rpc.remoting.netty.protocol.PlatformRemotingCommand;
import com.courage.platform.rpc.remoting.netty.protocol.PlatformRemotingSerializable;
import com.courage.platform.schedule.rpc.protocol.BaseCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

/**
 * 任务客户端
 * Created by zhangyong on 2018/10/3.
 */
public class ScheduleRpcClient implements ScheduleRpcService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleRpcClient.class);

    private PlatformNettyRemotingClient platformNettyRemotingClient;

    private static volatile boolean inited = false;

    public void start() {
        if (!inited) {
            PlatformNettyClientConfig platformNettyClientConfig = new PlatformNettyClientConfig();
            this.platformNettyRemotingClient = new PlatformNettyRemotingClient(platformNettyClientConfig);
            this.platformNettyRemotingClient.start();
            inited = true;
        }
    }

    public void registerProcessor(int requestCmd, PlatformNettyRequestProcessor processor, ExecutorService executor) {
        this.platformNettyRemotingClient.registerProcessor(requestCmd, processor, executor);
    }

    public PlatformRemotingCommand send(String remoteAddress, Integer requestCmd, BaseCommand scheduleCommand) throws Throwable {
        PlatformRemotingCommand requestCommand = new PlatformRemotingCommand();
        requestCommand.setRequestCmd(requestCmd);
        requestCommand.setTimestamp(System.currentTimeMillis());
        requestCommand.setBody(PlatformRemotingSerializable.encode(scheduleCommand));
        return this.platformNettyRemotingClient.invokeSync(remoteAddress, requestCommand, 3000L);
    }

    public void shutdown() {
        this.platformNettyRemotingClient.shutdown();
    }

}
