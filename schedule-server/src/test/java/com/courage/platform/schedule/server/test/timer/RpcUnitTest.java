package com.courage.platform.schedule.server.test.timer;

import com.courage.platform.rpc.remoting.exception.PlatformRemotingConnectException;
import com.courage.platform.rpc.remoting.exception.PlatformRemotingSendRequestException;
import com.courage.platform.rpc.remoting.exception.PlatformRemotingTimeoutException;
import com.courage.platform.rpc.remoting.netty.codec.PlatformNettyClientConfig;
import com.courage.platform.rpc.remoting.netty.codec.PlatformNettyRemotingClient;
import com.courage.platform.rpc.remoting.netty.protocol.PlatformRemotingCommand;
import com.courage.platform.rpc.remoting.netty.protocol.PlatformRemotingSerializable;
import com.courage.platform.schedule.rpc.protocol.CommandEnum;

/**
 * rpc 单元测试
 * Created by zhangyong on 2019/11/7.
 */
public class RpcUnitTest {

    public static void main(String[] args) throws InterruptedException, PlatformRemotingTimeoutException, PlatformRemotingConnectException, PlatformRemotingSendRequestException {
        PlatformNettyRemotingClient platformNettyRemotingClient = new PlatformNettyRemotingClient(new PlatformNettyClientConfig());
        platformNettyRemotingClient.start();
        PlatformRemotingCommand requestCommand = new PlatformRemotingCommand();
        requestCommand.putHeadParam("appName" , "com.courage.test");
        requestCommand.putHeadParam("appKey" , "123123");
        requestCommand.setRequestCmd(CommandEnum.REGISTER_CMD);
        requestCommand.setTimestamp(System.currentTimeMillis());
        requestCommand.setBody(PlatformRemotingSerializable.encode("mylife"));
        platformNettyRemotingClient.invokeSync("localhost:12999", requestCommand, 3000L);
        Thread.sleep(100000);
    }

}
