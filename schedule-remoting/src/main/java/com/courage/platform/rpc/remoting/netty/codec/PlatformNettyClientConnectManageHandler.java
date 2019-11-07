package com.courage.platform.rpc.remoting.netty.codec;

import com.courage.platform.rpc.remoting.common.PlatformRemotingHelper;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;

public class PlatformNettyClientConnectManageHandler extends ChannelDuplexHandler {

    private final static Logger log = LoggerFactory.getLogger(PlatformRemotingHelper.PLATFORM_REMOTING);

    private PlatformNettyRemotingClient platformNettyRemotingClient;

    public PlatformNettyClientConnectManageHandler(PlatformNettyRemotingClient platformNettyRemotingClient) {
        this.platformNettyRemotingClient = platformNettyRemotingClient;
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress,
                        ChannelPromise promise) throws Exception {
        final String local = localAddress == null ? "UNKNOWN" : PlatformRemotingHelper.parseSocketAddressAddr(localAddress);
        final String remote = remoteAddress == null ? "UNKNOWN" : PlatformRemotingHelper.parseSocketAddressAddr(remoteAddress);
        log.info("Platform NETTY CLIENT PIPELINE: CONNECT  {} => {}", local, remote);
        super.connect(ctx, remoteAddress, localAddress, promise);
        if (this.platformNettyRemotingClient.getChannelEventListener() != null) {
            this.platformNettyRemotingClient.nettyEventExecutor.putNettyEvent(new PlatformNettyEvent(
                    PlatformNettyEventType.CONNECT,
                    remote,
                    ctx.channel()));
        }
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        final String remoteAddress = PlatformRemotingHelper.parseChannelRemoteAddr(ctx.channel());
        log.info("Platform NETTY CLIENT PIPELINE: DISCONNECT {}", remoteAddress);
        this.platformNettyRemotingClient.closeChannel(ctx.channel());
        super.disconnect(ctx, promise);
        if (this.platformNettyRemotingClient.getChannelEventListener() != null) {
            this.platformNettyRemotingClient.nettyEventExecutor.putNettyEvent(
                    new PlatformNettyEvent(
                            PlatformNettyEventType.CLOSE,
                            remoteAddress,
                            ctx.channel()));
        }
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        final String remoteAddress = PlatformRemotingHelper.parseChannelRemoteAddr(ctx.channel());
        log.info("Platform NETTY CLIENT PIPELINE: CLOSE {}", remoteAddress);
        this.platformNettyRemotingClient.closeChannel(ctx.channel());
        super.close(ctx, promise);
        if (this.platformNettyRemotingClient.getChannelEventListener() != null) {
            this.platformNettyRemotingClient.nettyEventExecutor.putNettyEvent(new PlatformNettyEvent(PlatformNettyEventType.CLOSE, remoteAddress, ctx.channel()));
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state().equals(IdleState.ALL_IDLE)) {
                final String remoteAddress = PlatformRemotingHelper.parseChannelRemoteAddr(ctx.channel());
                log.warn("Platform NETTY CLIENT PIPELINE: IDLE exception [{}]", remoteAddress);
                this.platformNettyRemotingClient.closeChannel(ctx.channel());
                if (this.platformNettyRemotingClient.getChannelEventListener() != null) {
                    this.platformNettyRemotingClient.nettyEventExecutor.putNettyEvent(new PlatformNettyEvent(PlatformNettyEventType.IDLE, remoteAddress, ctx.channel()));
                }
            }
        }

        ctx.fireUserEventTriggered(evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        final String remoteAddress = PlatformRemotingHelper.parseChannelRemoteAddr(ctx.channel());
        log.warn("Platform NETTY CLIENT PIPELINE: exceptionCaught {}", remoteAddress);
        log.warn("Platform NETTY CLIENT PIPELINE: exceptionCaught exception.", cause);
        this.platformNettyRemotingClient.closeChannel(ctx.channel());
        if (this.platformNettyRemotingClient.getChannelEventListener() != null) {
            this.platformNettyRemotingClient.nettyEventExecutor.putNettyEvent(new PlatformNettyEvent(PlatformNettyEventType.EXCEPTION, remoteAddress, ctx.channel()));
        }
    }

}
