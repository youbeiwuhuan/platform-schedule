package com.courage.platform.schedule.server.rpc;

import io.netty.channel.Channel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * rpc 通道管理
 * Created by zhangyong on 2019/11/7.
 */
@Component
public class RpcChannelManager {

    private final static String CHANNEL_ID_KEY = "channelId";

    /*
      channelId ----> RpcChannelSession
     */
    private HashMap<String, RpcChannelSession> sessionHashMap = new HashMap<>(4048);

    /*
       appName ----> Set<ChannelId>
     */
    private HashMap<String, Set<String>> appNameChannelIdMapping = new HashMap<>(4048);

    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public List<RpcChannelSession> getChannelSessionListByAppName(String appName) {
        readWriteLock.readLock().lock();
        try {
            Set<String> channelIdSet = appNameChannelIdMapping.get(appName);
            if (CollectionUtils.isEmpty(channelIdSet)) {
                return Collections.emptyList();
            }
            List<RpcChannelSession> dataList = new ArrayList<>(channelIdSet.size());
            for (String channelId : channelIdSet) {
                RpcChannelSession rpcChannelSession = sessionHashMap.get(channelId);
                if (rpcChannelSession != null) {
                    dataList.add(rpcChannelSession);
                }
            }
            return dataList;
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    public Map<String, Object> searchOnlineApp(int start, int pageSize) {
        readWriteLock.readLock().lock();
        try {

        } finally {
            readWriteLock.readLock().unlock();
        }
        return MapUtils.EMPTY_SORTED_MAP;
    }

    public void bindChannelId(Channel channel) {
        Long channelId = ChannelUtils.getChannelId();
        ChannelUtils.putAttr(CHANNEL_ID_KEY, String.valueOf(channelId), channel);
    }

    public RpcChannelSession createChannelSession(Channel channel, String appName, String clientId) {
        readWriteLock.writeLock().lock();
        try {
            String channelId = (String) ChannelUtils.getAttr(CHANNEL_ID_KEY, channel);
            RpcChannelSession rpcChannelSession = sessionHashMap.get(channelId);
            if (rpcChannelSession == null) {
                rpcChannelSession = new RpcChannelSession(channelId, channel, appName, clientId);
                sessionHashMap.put(channelId, rpcChannelSession);
                Set<String> channelIdSet = appNameChannelIdMapping.get(appName);
                if (channelIdSet == null) {
                    channelIdSet = new HashSet<>();
                    appNameChannelIdMapping.put(appName, channelIdSet);
                }
                channelIdSet.add(channelId);
            } else {
                //修改心跳信息
                rpcChannelSession.setUpdateTime(System.currentTimeMillis());
            }
            return rpcChannelSession;
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    public void closeChannelSession(Channel channel) {
        readWriteLock.writeLock().lock();
        try {
            String channelId = (String) ChannelUtils.getAttr(CHANNEL_ID_KEY, channel);
            RpcChannelSession rpcChannelSession = sessionHashMap.get(channelId);
            if (rpcChannelSession != null) {
                Set<String> channelIdSet = appNameChannelIdMapping.get(rpcChannelSession.getAppName());
                if (CollectionUtils.isNotEmpty(channelIdSet)) {
                    channelIdSet.remove(channelId);
                }
                sessionHashMap.remove(channelId);
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

}
