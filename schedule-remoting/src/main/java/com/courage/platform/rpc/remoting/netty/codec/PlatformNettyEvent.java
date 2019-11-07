package com.courage.platform.rpc.remoting.netty.codec;

import io.netty.channel.Channel;

public class PlatformNettyEvent {

    private final PlatformNettyEventType type;

    private final String remoteAddr;

    private final Channel channel;

    public PlatformNettyEvent(PlatformNettyEventType type, String remoteAddr, Channel channel) {
        this.type = type;
        this.remoteAddr = remoteAddr;
        this.channel = channel;
    }

    public PlatformNettyEventType getType() {
        return type;
    }

    public String getRemoteAddr() {
        return remoteAddr;
    }

    public Channel getChannel() {
        return channel;
    }

    @Override
    public String toString() {
        return "PlatformNettyEvent [type=" + type + ", remoteAddr=" + remoteAddr + ", channel=" + channel + "]";
    }

}
