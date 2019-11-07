package com.courage.platform.schedule.server.rpc;

import com.courage.platform.rpc.remoting.PlatformChannelEventListener;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * rpc channel 变化监听器
 * Created by zhangyong on 2019/11/7.
 */
public class RpcChannelListener implements PlatformChannelEventListener {

    private static final Logger logger = LoggerFactory.getLogger(RpcChannelListener.class);

    @Override
    public void onChannelConnect(String s, Channel channel) {

    }

    @Override
    public void onChannelClose(String s, Channel channel) {

    }

    @Override
    public void onChannelException(String s, Channel channel) {

    }

    @Override
    public void onChannelIdle(String s, Channel channel) {

    }

}
