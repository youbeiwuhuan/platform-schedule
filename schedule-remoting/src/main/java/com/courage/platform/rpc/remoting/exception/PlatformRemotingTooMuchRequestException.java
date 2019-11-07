package com.courage.platform.rpc.remoting.exception;

public class PlatformRemotingTooMuchRequestException extends PlatformRemotingException {
    private static final long serialVersionUID = 4326919581254519654L;

    public PlatformRemotingTooMuchRequestException(String message) {
        super(message);
    }
}
