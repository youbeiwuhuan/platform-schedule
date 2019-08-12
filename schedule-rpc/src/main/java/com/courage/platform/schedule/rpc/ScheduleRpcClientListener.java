package com.courage.platform.schedule.rpc;

import com.courage.platform.rpc.remoting.PlatformChannelEventListener;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 客户端空闲监听器 (若发生空闲链接，则发送心跳请求)
 * 若发送心跳后,server没有返回结果
 */
public class ScheduleRpcClientListener implements PlatformChannelEventListener {

    private final static Logger logger = LoggerFactory.getLogger(ScheduleRpcClientListener.class);

    @Override
    public void onChannelConnect(String remoteAddr, Channel channel) {
    }

    @Override
    public void onChannelClose(String remoteAddr, Channel channel) {
    }

    @Override
    public void onChannelException(String remoteAddr, Channel channel) {

    }

    @Override
    public void onChannelIdle(String remoteAddr, Channel channel) {

    }

}
