package com.courage.platform.schedule.server.rpc.processor;

import com.alibaba.fastjson.JSON;
import com.courage.platform.rpc.remoting.netty.codec.PlatformNettyRequestProcessor;
import com.courage.platform.rpc.remoting.netty.protocol.PlatformRemotingCommand;
import com.courage.platform.schedule.rpc.protocol.ConsoleOnlineAppCommand;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 在线应用查询处理器
 * Created by zhangyong on 2019/11/15.
 */
public class ConsoleOnlineAppProcessor implements PlatformNettyRequestProcessor {

    private final static Logger logger = LoggerFactory.getLogger(ConsoleOnlineAppProcessor.class);

    @Override
    public PlatformRemotingCommand processRequest(ChannelHandlerContext ctx, PlatformRemotingCommand request) throws Exception {
        byte[] bytes = request.getBody();
        ConsoleOnlineAppCommand consoleOnlineAppCommand = JSON.parseObject(bytes, ConsoleOnlineAppCommand.class);
        return null;
    }

    @Override
    public boolean rejectRequest() {
        return false;
    }

}
