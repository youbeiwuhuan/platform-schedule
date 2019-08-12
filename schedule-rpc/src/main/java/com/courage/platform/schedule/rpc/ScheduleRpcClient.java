package com.courage.platform.schedule.rpc;

import com.alibaba.fastjson.JSON;
import com.courage.platform.rpc.remoting.PlatformRemotingClient;
import com.courage.platform.rpc.remoting.netty.codec.PlatformNettyClientConfig;
import com.courage.platform.rpc.remoting.netty.codec.PlatformNettyRemotingClient;
import com.courage.platform.rpc.remoting.netty.protocol.PlatformRemotingCommand;
import com.courage.platform.rpc.remoting.netty.protocol.PlatformRemotingSerializable;
import com.courage.platform.schedule.rpc.protocol.BaseCommand;
import com.courage.platform.schedule.rpc.protocol.CommandEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 任务客户端
 * Created by zhangyong on 2018/10/3.
 */
public class ScheduleRpcClient implements ScheduleRpcService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleRpcClient.class);

    private PlatformNettyRemotingClient platformNettyRemotingClient;

    private static ScheduleRpcClient instance = new ScheduleRpcClient();

    public static ScheduleRpcClient getSingleInstance() {
        return instance;
    }

    private static volatile boolean inited = false;

    public void start() {
        if (!inited) {
            PlatformNettyClientConfig platformNettyClientConfig = new PlatformNettyClientConfig();
            this.platformNettyRemotingClient = new PlatformNettyRemotingClient(platformNettyClientConfig);
            this.platformNettyRemotingClient.start();
            inited = true;
        }

    }

    private ScheduleRpcClient() {
        start();
    }


    public PlatformRemotingCommand send(String remoteAddress, Integer requestCmd, BaseCommand scheduleCommand) throws Throwable {
        send(remoteAddress, platformNettyRemotingClient, JSON.toJSONString(scheduleCommand));
        PlatformRemotingCommand requestCommand = new PlatformRemotingCommand();
        requestCommand.setRequestCmd(requestCmd);
        requestCommand.setTimestamp(System.currentTimeMillis());
        requestCommand.setBody(PlatformRemotingSerializable.encode(scheduleCommand));
        return this.platformNettyRemotingClient.invokeSync(remoteAddress, requestCommand, 3000L);
    }

    private void send(String currentAddr, PlatformRemotingClient platformRemotingClient, String commandInfo) throws InterruptedException {
        try {
            PlatformRemotingCommand platformRemotingCommand = new PlatformRemotingCommand();
            platformRemotingCommand.setRequestCmd(CommandEnum.SCHEDULE_HEART_BEAT_CMD);
            platformRemotingClient.invokeSync(currentAddr, platformRemotingCommand, 2 * 1000);
        } catch (Exception e) {
            logger.error("发送请求前探测心跳失败，调用地址：" + currentAddr + "，请求参数：" + commandInfo, e);
            try {
                Thread.sleep(50);
            } catch (Exception e1) {
            }
        }
    }

    public void shutdown() {
        this.platformNettyRemotingClient.shutdown();
    }

}
