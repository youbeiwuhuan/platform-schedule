package com.courage.platform.rpc.remoting;

import com.courage.platform.rpc.remoting.exception.PlatformRemotingConnectException;
import com.courage.platform.rpc.remoting.exception.PlatformRemotingSendRequestException;
import com.courage.platform.rpc.remoting.exception.PlatformRemotingTimeoutException;
import com.courage.platform.rpc.remoting.exception.PlatformRemotingTooMuchRequestException;
import com.courage.platform.rpc.remoting.netty.codec.PlatformNettyRequestProcessor;
import com.courage.platform.rpc.remoting.netty.protocol.PlatformRemotingCommand;

import java.util.concurrent.ExecutorService;

public interface PlatformRemotingClient extends PlatformRemotingService {

    PlatformRemotingCommand invokeSync(final String addr, final PlatformRemotingCommand request, final long timeoutMillis) throws InterruptedException, PlatformRemotingConnectException, PlatformRemotingSendRequestException, PlatformRemotingTimeoutException;

    void invokeAsync(final String addr, final PlatformRemotingCommand request, final long timeoutMillis, final PlatformInvokeCallback invokeCallback) throws InterruptedException, PlatformRemotingConnectException, PlatformRemotingTooMuchRequestException, PlatformRemotingTimeoutException, PlatformRemotingSendRequestException;

    void invokeOneway(final String addr, final PlatformRemotingCommand request, final long timeoutMillis)
            throws InterruptedException, PlatformRemotingConnectException, PlatformRemotingTooMuchRequestException, PlatformRemotingTimeoutException, PlatformRemotingSendRequestException;

    void registerProcessor(final int requestCmd, final PlatformNettyRequestProcessor processor, final ExecutorService executor);

    void setCallbackExecutor(final ExecutorService callbackExecutor);

    boolean isChannelWritable(final String addr);

}
