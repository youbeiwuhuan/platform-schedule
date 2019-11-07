package com.courage.platform.schedule.server.rpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * rpc 通道管理
 * Created by zhangyong on 2019/11/7.
 */
@Component
public class RpcChannelManager {

    private static final Logger logger = LoggerFactory.getLogger(RpcChannelManager.class);

    private HashMap<Long, RpcChannelSession> channelConcurrentHashMap = new HashMap<>(4048);

    private HashMap<String, Set<Long>> appNameChannelIdMapping = new HashMap<>(4048);

    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public List<RpcChannelSession> getChannelSessionListByAppName(String appName) {
        return null;
    }

}
