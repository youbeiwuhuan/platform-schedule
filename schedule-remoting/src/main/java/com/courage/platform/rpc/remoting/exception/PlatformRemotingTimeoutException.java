package com.courage.platform.rpc.remoting.exception;

public class PlatformRemotingTimeoutException extends PlatformRemotingException {

    private static final long serialVersionUID = 4106899185095245979L;

    public PlatformRemotingTimeoutException(String message) {
        super(message);
    }

    public PlatformRemotingTimeoutException(String addr, long timeoutMillis) {
        this(addr, timeoutMillis, null);
    }

    public PlatformRemotingTimeoutException(String addr, long timeoutMillis, Throwable cause) {
        super("wait response on the channel <" + addr + "> timeout, " + timeoutMillis + "(ms)", cause);
    }
}
