package com.courage.platform.rpc.remoting.exception;

public class PlatformRemotingException extends Exception {
    private static final long serialVersionUID = -5690687334570505110L;

    public PlatformRemotingException(String message) {
        super(message);
    }

    public PlatformRemotingException(String message, Throwable cause) {
        super(message, cause);
    }
}
