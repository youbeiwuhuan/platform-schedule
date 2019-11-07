package com.courage.platform.rpc.remoting.netty.codec;

import com.courage.platform.rpc.remoting.common.PlatformRemotingHelper;
import com.courage.platform.rpc.remoting.netty.protocol.PlatformRemotingCommand;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlatformNettyServerHandler extends SimpleChannelInboundHandler<PlatformRemotingCommand> {

    private final static Logger logger = LoggerFactory.getLogger(PlatformRemotingHelper.PLATFORM_REMOTING);

    private PlatformNettyRemotingAbstract platformNettyRemotingAbstract;

    public PlatformNettyServerHandler(PlatformNettyRemotingAbstract platformNettyRemotingAbstract) {
        this.platformNettyRemotingAbstract = platformNettyRemotingAbstract;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, PlatformRemotingCommand platformRemotingCommand) throws Exception {
        platformNettyRemotingAbstract.processMessageReceived(channelHandlerContext, platformRemotingCommand);
    }

}
