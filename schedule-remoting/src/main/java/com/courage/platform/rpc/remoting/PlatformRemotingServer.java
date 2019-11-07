package com.courage.platform.rpc.remoting;

import com.courage.platform.rpc.remoting.common.PlatformPair;
import com.courage.platform.rpc.remoting.exception.PlatformRemotingSendRequestException;
import com.courage.platform.rpc.remoting.exception.PlatformRemotingTimeoutException;
import com.courage.platform.rpc.remoting.exception.PlatformRemotingTooMuchRequestException;
import com.courage.platform.rpc.remoting.netty.codec.PlatformNettyRequestProcessor;
import com.courage.platform.rpc.remoting.netty.protocol.PlatformRemotingCommand;
import io.netty.channel.Channel;

import java.util.concurrent.ExecutorService;

public interface PlatformRemotingServer extends PlatformRemotingService {

    void registerProcessor(final int requestCmd, final PlatformNettyRequestProcessor processor, final ExecutorService executor);

    void registerDefaultProcessor(final PlatformNettyRequestProcessor processor, final ExecutorService executor);

    int localListenPort();

    PlatformPair<PlatformNettyRequestProcessor, ExecutorService> getProcessorPair(final int requestCode);

    PlatformRemotingCommand invokeSync(final Channel channel, final PlatformRemotingCommand request, final long timeoutMillis) throws InterruptedException, PlatformRemotingSendRequestException,
            PlatformRemotingTimeoutException;

    void invokeAsync(final Channel channel, final PlatformRemotingCommand request, final long timeoutMillis, final PlatformInvokeCallback invokeCallback) throws InterruptedException,
            PlatformRemotingTooMuchRequestException, PlatformRemotingTimeoutException, PlatformRemotingSendRequestException;

    void invokeOneway(final Channel channel, final PlatformRemotingCommand request, final long timeoutMillis)
            throws InterruptedException, PlatformRemotingTooMuchRequestException, PlatformRemotingTimeoutException,
            PlatformRemotingSendRequestException;

}
