package com.courage.platform.rpc.remoting.netty.protocol;

import com.alibaba.fastjson.JSON;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class PlatformRemotingCommandUtils {

    private final static Charset CHARSET_UTF8 = Charset.forName("UTF-8");

    public static PlatformRemotingCommand decode(final ByteBuffer byteBuffer) {
        int length = byteBuffer.limit();
        int oriHeaderLen = byteBuffer.getInt();
        int headerLength = getHeaderLength(oriHeaderLen);
        byte[] headerData = new byte[headerLength];
        byteBuffer.get(headerData);
        String headerJson = new String(headerData, CHARSET_UTF8);
        PlatformRemotingCommand platformRemotingCommand = JSON.parseObject(headerJson, PlatformRemotingCommand.class);
        int bodyLength = length - 4 - headerLength;
        byte[] bodyData = null;
        if (bodyLength > 0) {
            bodyData = new byte[bodyLength];
            byteBuffer.get(bodyData);
        }
        platformRemotingCommand.setBody(bodyData);
        return platformRemotingCommand;
    }

    public static int getHeaderLength(int length) {
        return length & 0xFFFFFF;
    }

    public static ByteBuffer encode(PlatformRemotingCommand platformRemotingCommand) {
        // 2> header data length
        byte[] headerData = JSON.toJSONBytes(platformRemotingCommand);
        // 3> body data length
        int bodyLength = platformRemotingCommand.getBody() == null ? 0 : platformRemotingCommand.getBody().length;
        //前4个字节存储 4 + headerLength + bodyLength
        int totalLength = 4 + headerData.length + bodyLength;
        ByteBuffer result = ByteBuffer.allocate(totalLength + 4);
        // length
        result.putInt(totalLength);
        // header length
        result.putInt(headerData.length);
        // header data
        result.put(headerData);
        // body data
        if (platformRemotingCommand.getBody() != null) {
            result.put(platformRemotingCommand.getBody());
        }
        result.flip();
        return result;
    }

    public static PlatformRemotingCommand createResponseCommand(int code, String remark) {
        PlatformRemotingCommand platformRemotingCommand = new PlatformRemotingCommand();
        platformRemotingCommand.setCode(code);
        platformRemotingCommand.setRemark(remark);
        platformRemotingCommand.setFormat(PlatformRemotingCommandFormat.RESPONSE.getCode());
        return platformRemotingCommand;
    }


}
