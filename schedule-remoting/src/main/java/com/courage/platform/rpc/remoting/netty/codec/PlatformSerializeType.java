package com.courage.platform.rpc.remoting.netty.codec;

public enum PlatformSerializeType {

    JSON((byte) 0),

    CUSTOM((byte) 1);

    private byte code;

    PlatformSerializeType(byte code) {
        this.code = code;
    }

    public static PlatformSerializeType valueOf(byte code) {
        for (PlatformSerializeType serializeType : PlatformSerializeType.values()) {
            if (serializeType.getCode() == code) {
                return serializeType;
            }
        }
        return null;
    }

    public byte getCode() {
        return code;
    }

}
