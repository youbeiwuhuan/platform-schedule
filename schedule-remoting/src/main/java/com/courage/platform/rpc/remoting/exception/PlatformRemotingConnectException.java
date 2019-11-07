package com.courage.platform.rpc.remoting.exception;

public class PlatformRemotingConnectException extends PlatformRemotingException {
    private static final long serialVersionUID = -5565366231695911316L;

    public PlatformRemotingConnectException(String addr) {
        this(addr, null);
    }

    public PlatformRemotingConnectException(String addr, Throwable cause) {
        super("connect to <" + addr + "> failed", cause);
    }
}
