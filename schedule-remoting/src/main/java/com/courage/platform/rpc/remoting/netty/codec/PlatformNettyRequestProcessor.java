package com.courage.platform.rpc.remoting.netty.codec;

import com.courage.platform.rpc.remoting.netty.protocol.PlatformRemotingCommand;
import io.netty.channel.ChannelHandlerContext;

/**
 * Common remoting command processor
 */
public interface PlatformNettyRequestProcessor {

    PlatformRemotingCommand processRequest(ChannelHandlerContext ctx, PlatformRemotingCommand request)
            throws Exception;

    boolean rejectRequest();

}
