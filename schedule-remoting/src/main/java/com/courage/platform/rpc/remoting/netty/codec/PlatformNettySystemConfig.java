package com.courage.platform.rpc.remoting.netty.codec;

public class PlatformNettySystemConfig {
    public static final String COM_HSHC_PLATFORM_REMOTING_NETTY_POOLED_BYTE_BUF_ALLOCATOR_ENABLE =
        "com.courage.platform.rpc.remoting.nettyPooledByteBufAllocatorEnable";
    public static final String COM_HSHC_PLATFORM_REMOTING_SOCKET_SNDBUF_SIZE =
        "com.courage.platform.rpc.remoting.socket.sndbuf.size";
    public static final String COM_HSHC_PLATFORM_REMOTING_SOCKET_RCVBUF_SIZE =
        "com.courage.platform.rpc.remoting.socket.rcvbuf.size";
    public static final String COM_HSHC_PLATFORM_REMOTING_CLIENT_ASYNC_SEMAPHORE_VALUE =
        "com.courage.platform.rpc.remoting.clientAsyncSemaphoreValue";
    public static final String COM_HSHC_PLATFORM_REMOTING_CLIENT_ONEWAY_SEMAPHORE_VALUE =
        "com.courage.platform.rpc.remoting.clientOnewaySemaphoreValue";

    public static final boolean NETTY_POOLED_BYTE_BUF_ALLOCATOR_ENABLE = //
        Boolean.parseBoolean(System.getProperty(COM_HSHC_PLATFORM_REMOTING_NETTY_POOLED_BYTE_BUF_ALLOCATOR_ENABLE, "false"));
    public static final int CLIENT_ASYNC_SEMAPHORE_VALUE = //
        Integer.parseInt(System.getProperty(COM_HSHC_PLATFORM_REMOTING_CLIENT_ASYNC_SEMAPHORE_VALUE, "65535"));
    public static final int CLIENT_ONEWAY_SEMAPHORE_VALUE =
        Integer.parseInt(System.getProperty(COM_HSHC_PLATFORM_REMOTING_CLIENT_ONEWAY_SEMAPHORE_VALUE, "65535"));
    public static int socketSndbufSize =
        Integer.parseInt(System.getProperty(COM_HSHC_PLATFORM_REMOTING_SOCKET_SNDBUF_SIZE, "65535"));
    public static int socketRcvbufSize =
        Integer.parseInt(System.getProperty(COM_HSHC_PLATFORM_REMOTING_SOCKET_RCVBUF_SIZE, "65535"));
}
