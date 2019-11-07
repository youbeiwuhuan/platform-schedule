package com.courage.platform.rpc.remoting;

import com.courage.platform.rpc.remoting.netty.codec.PlatformResponseFuture;

public interface PlatformInvokeCallback {

    void operationComplete(final PlatformResponseFuture responseFuture);

}
