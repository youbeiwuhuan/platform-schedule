package com.courage.platform.rpc.remoting.exception;

public class PlatformRemotingCommandException extends PlatformRemotingException {
    private static final long serialVersionUID = -6061365915274953096L;

    public PlatformRemotingCommandException(String message) {
        super(message, null);
    }

    public PlatformRemotingCommandException(String message, Throwable cause) {
        super(message, cause);
    }
}
