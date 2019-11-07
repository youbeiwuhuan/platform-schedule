package com.courage.platform.rpc.remoting.netty.protocol;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class PlatformRemotingCommand {

    //响应码
    private int code = PlatformRemotingSysResponseCode.SUCCESS;

    private static AtomicInteger requestId = new AtomicInteger(0);

    //format 整数 请求无需返回0  请求传1 响应传2
    private int format = PlatformRemotingCommandFormat.REQUESET.getCode();

    private int opaque = requestId.getAndIncrement();

    private int requestCmd;

    private Long timestamp = System.currentTimeMillis();

    private String remark;

    private transient byte[] body;

    private Map headParams;

    public Object getHeadParam(final Object key) {
        if (headParams == null) {
            return null;
        }
        return headParams.get(key);
    }

    public void putHeadParam(final Object key, final Object value) {
        if (headParams == null) {
            headParams = new HashMap();
        }
        headParams.put(key, value);
    }

    public static AtomicInteger getRequestId() {
        return requestId;
    }

    public static void setRequestId(AtomicInteger requestId) {
        PlatformRemotingCommand.requestId = requestId;
    }

    public Map getHeadParams() {
        return headParams;
    }

    public void setHeadParams(Map headParams) {
        this.headParams = headParams;
    }

    public int getOpaque() {
        return opaque;
    }

    public void setOpaque(int opaque) {
        this.opaque = opaque;
    }

    public int getRequestCmd() {
        return requestCmd;
    }

    public void setRequestCmd(int requestCmd) {
        this.requestCmd = requestCmd;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getFormat() {
        return format;
    }

    public void setFormat(int format) {
        this.format = format;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "PlatformRemotingCommand{" +
                "code=" + code +
                ", format=" + format +
                ", opaque=" + opaque +
                ", requestCmd=" + requestCmd +
                ", timestamp=" + timestamp +
                ", remark='" + remark + '\'' +
                ", body=" + Arrays.toString(body) +
                '}';
    }
}
