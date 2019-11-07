package com.courage.platform.schedule.server.rpc;

import com.courage.platform.rpc.remoting.PlatformChannelEventListener;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

/**
 * rpc channel 变化监听器
 * Created by zhangyong on 2019/11/7.
 */
public class RpcChannelListener implements PlatformChannelEventListener {

    private static final Logger logger = LoggerFactory.getLogger(RpcChannelListener.class);

    private final static AtomicLong channelId = new AtomicLong(0);

    @Override
    public void onChannelConnect(String remoteAddr, Channel channel) {
        logger.info("远程链接:" + remoteAddr + "链接上了");
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

    public static Long getChannelId() {
        return channelId.getAndIncrement();
    }


}
