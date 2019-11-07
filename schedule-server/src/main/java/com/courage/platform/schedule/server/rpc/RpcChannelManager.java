package com.courage.platform.schedule.server.rpc;

import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * rpc 通道管理
 * Created by zhangyong on 2019/11/7.
 */
@Component
public class RpcChannelManager {

    private static final Logger logger = LoggerFactory.getLogger(RpcChannelManager.class);

    private static ConcurrentHashMap<Long, Channel> channelConcurrentHashMap = new ConcurrentHashMap<>(4048);

}
