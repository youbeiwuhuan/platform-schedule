package com.courage.platform.rpc.remoting.netty.codec;

import com.courage.platform.rpc.remoting.netty.protocol.PlatformRemotingCommand;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlatformNettyClientHandler extends SimpleChannelInboundHandler<PlatformRemotingCommand> {

    private final static Logger logger = LoggerFactory.getLogger(PlatformNettyServerHandler.class);

    private PlatformNettyRemotingClient platformNettyRemotingClient;

    public PlatformNettyClientHandler(PlatformNettyRemotingClient platformNettyRemotingClient) {
        this.platformNettyRemotingClient = platformNettyRemotingClient;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PlatformRemotingCommand msg) throws Exception {
        platformNettyRemotingClient.processMessageReceived(ctx, msg);
    }

}
