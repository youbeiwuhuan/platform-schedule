package com.courage.platform.rpc.remoting;

import com.courage.platform.rpc.remoting.netty.protocol.PlatformRemotingCommand;

public interface PlatformRpcHook {

    void doBeforeRequest(final String remoteAddr, final PlatformRemotingCommand request);

    void doAfterResponse(final String remoteAddr, final PlatformRemotingCommand request, final PlatformRemotingCommand response);

}
