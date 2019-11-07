package com.courage.platform.schedule.server.rpc;

import io.netty.channel.Channel;

/**
 * 链路对象
 * Created by zhangyong on 2019/11/7.
 */
public class RpcChannelSession {

    private String clientId;

    private Long channelId;

    private Channel channel;

    private long createTime = System.currentTimeMillis();

    public RpcChannelSession(Long channelId, Channel channel) {
        this.channelId = channelId;
        this.channel = channel;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

}
