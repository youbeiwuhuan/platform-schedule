package com.courage.platform.rpc.remoting.exception;

public class PlatformRemotingSendRequestException extends PlatformRemotingException {

    private static final long serialVersionUID = 5391285827332471674L;

    public PlatformRemotingSendRequestException(String addr) {
        this(addr, null);
    }

    public PlatformRemotingSendRequestException(String addr, Throwable cause) {
        super("send request to <" + addr + "> failed", cause);
    }

}
