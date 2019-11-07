package com.courage.platform.rpc.remoting.netty.codec;

import com.courage.platform.rpc.remoting.common.PlatformRemotingHelper;
import com.courage.platform.rpc.remoting.common.PlatformRemotingUtil;
import com.courage.platform.rpc.remoting.netty.protocol.PlatformRemotingCommandUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class PlatformNettyDecoder extends LengthFieldBasedFrameDecoder {

    private static final Logger logger = LoggerFactory.getLogger(PlatformRemotingHelper.PLATFORM_REMOTING);

    private static final int FRAME_MAX_LENGTH = Integer.parseInt(System.getProperty("com.courage.platform.rpc.remoting.frameMaxLength", "8388608"));

    public PlatformNettyDecoder() {
        super(FRAME_MAX_LENGTH, 0, 4, 0, 4);
    }

    @Override
    public Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = null;
        try {
            frame = (ByteBuf) super.decode(ctx, in);
            if (null == frame) {
                return null;
            }
            ByteBuffer byteBuffer = frame.nioBuffer();
            return PlatformRemotingCommandUtils.decode(byteBuffer);
        } catch (Exception e) {
            logger.error("decode exception, " + PlatformRemotingHelper.parseChannelRemoteAddr(ctx.channel()), e);
            PlatformRemotingUtil.closeChannel(ctx.channel());
        } finally {
            if (null != frame) {
                frame.release();
            }
        }
        return null;
    }

}
