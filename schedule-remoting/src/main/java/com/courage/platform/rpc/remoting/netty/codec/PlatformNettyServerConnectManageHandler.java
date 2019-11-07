package com.courage.platform.rpc.remoting.netty.codec;

import com.courage.platform.rpc.remoting.common.PlatformRemotingHelper;
import com.courage.platform.rpc.remoting.common.PlatformRemotingUtil;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlatformNettyServerConnectManageHandler extends ChannelDuplexHandler {

    private final static Logger log = LoggerFactory.getLogger(PlatformRemotingHelper.PLATFORM_REMOTING);

    private PlatformNettyRemotingAbstract platformNettyRemotingAbstract;

    public PlatformNettyServerConnectManageHandler(PlatformNettyRemotingAbstract platformNettyRemotingAbstract) {
        this.platformNettyRemotingAbstract = platformNettyRemotingAbstract;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        final String remoteAddress = PlatformRemotingHelper.parseChannelRemoteAddr(ctx.channel());
        log.info("PlatformNETTY SERVER PIPELINE: channelRegistered {}", remoteAddress);
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        final String remoteAddress = PlatformRemotingHelper.parseChannelRemoteAddr(ctx.channel());
        log.info("PlatformNETTY SERVER PIPELINE: channelUnregistered, the channel[{}]", remoteAddress);
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        final String remoteAddress = PlatformRemotingHelper.parseChannelRemoteAddr(ctx.channel());
        log.info("PlatformNETTY SERVER PIPELINE: channelActive, the channel[{}]", remoteAddress);
        super.channelActive(ctx);
        if (this.platformNettyRemotingAbstract.getChannelEventListener() != null) {
            this.platformNettyRemotingAbstract.nettyEventExecutor.putNettyEvent(new PlatformNettyEvent(PlatformNettyEventType.CONNECT, remoteAddress, ctx.channel()));
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        final String remoteAddress = PlatformRemotingHelper.parseChannelRemoteAddr(ctx.channel());
        log.info("PlatformNETTY SERVER PIPELINE: channelInactive, the channel[{}]", remoteAddress);
        super.channelInactive(ctx);
        if (this.platformNettyRemotingAbstract.getChannelEventListener() != null) {
            this.platformNettyRemotingAbstract.nettyEventExecutor.putNettyEvent(new PlatformNettyEvent(PlatformNettyEventType.CLOSE, remoteAddress, ctx.channel()));
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state().equals(IdleState.ALL_IDLE)) {
                final String remoteAddress = PlatformRemotingHelper.parseChannelRemoteAddr(ctx.channel());
                log.warn("PlatformNETTY SERVER PIPELINE: IDLE exception [{}]", remoteAddress);
                PlatformRemotingUtil.closeChannel(ctx.channel());
                if (this.platformNettyRemotingAbstract.getChannelEventListener() != null) {
                    this.platformNettyRemotingAbstract.nettyEventExecutor.putNettyEvent(new PlatformNettyEvent(PlatformNettyEventType.IDLE, remoteAddress, ctx.channel()));
                }
            }
        }
        ctx.fireUserEventTriggered(evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        final String remoteAddress = PlatformRemotingHelper.parseChannelRemoteAddr(ctx.channel());
        log.warn("PlatformNETTY SERVER PIPELINE: exceptionCaught {}", remoteAddress);
        log.warn("PlatformNETTY SERVER PIPELINE: exceptionCaught exception.", cause);
        if (this.platformNettyRemotingAbstract.getChannelEventListener() != null) {
            this.platformNettyRemotingAbstract.nettyEventExecutor.putNettyEvent(new PlatformNettyEvent(PlatformNettyEventType.EXCEPTION, remoteAddress, ctx.channel()));
        }
        PlatformRemotingUtil.closeChannel(ctx.channel());
    }

}
