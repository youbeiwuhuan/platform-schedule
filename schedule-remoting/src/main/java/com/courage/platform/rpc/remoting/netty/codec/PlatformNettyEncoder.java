package com.courage.platform.rpc.remoting.netty.codec;

import com.courage.platform.rpc.remoting.common.PlatformRemotingHelper;
import com.courage.platform.rpc.remoting.common.PlatformRemotingUtil;
import com.courage.platform.rpc.remoting.netty.protocol.PlatformRemotingCommand;
import com.courage.platform.rpc.remoting.netty.protocol.PlatformRemotingCommandUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class PlatformNettyEncoder extends MessageToByteEncoder<PlatformRemotingCommand> {

    private static final Logger log = LoggerFactory.getLogger(PlatformRemotingHelper.PLATFORM_REMOTING);

    @Override
    public void encode(ChannelHandlerContext ctx, PlatformRemotingCommand platformRemotingCommand, ByteBuf out) throws Exception {
        try {
            ByteBuffer byteBuffer = PlatformRemotingCommandUtils.encode(platformRemotingCommand);
            out.writeBytes(byteBuffer);
        } catch (Exception e) {
            log.error("encode exception, " + PlatformRemotingHelper.parseChannelRemoteAddr(ctx.channel()), e);
            if (platformRemotingCommand != null) {
                log.error(platformRemotingCommand.toString());
            }
            PlatformRemotingUtil.closeChannel(ctx.channel());
        }
    }

}
