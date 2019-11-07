package com.courage.platform.rpc.remoting;

public interface PlatformRemotingService {

    void start();

    void shutdown();

    void registerRpcHook(PlatformRpcHook rpcHook);

}
