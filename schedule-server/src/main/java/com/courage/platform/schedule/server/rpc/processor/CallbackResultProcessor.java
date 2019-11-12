package com.courage.platform.schedule.server.rpc.processor;

import com.alibaba.fastjson.JSON;
import com.courage.platform.rpc.remoting.netty.codec.PlatformNettyRequestProcessor;
import com.courage.platform.rpc.remoting.netty.protocol.PlatformRemotingCommand;
import com.courage.platform.schedule.dao.ScheduleJobLogDao;
import com.courage.platform.schedule.rpc.protocol.CallbackCommand;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

/*
   回调任务处理结果处理器(client发送命令给server)
 */
public class CallbackResultProcessor implements PlatformNettyRequestProcessor {

    private static final Logger logger = LoggerFactory.getLogger(CallbackResultProcessor.class);

    @Autowired
    private ScheduleJobLogDao scheduleJobLogDao;

    @Override
    public PlatformRemotingCommand processRequest(ChannelHandlerContext channelHandlerContext, PlatformRemotingCommand platformRemotingCommand) throws Exception {
        byte[] bytes = platformRemotingCommand.getBody();
        CallbackCommand callbackCommand = JSON.parseObject(bytes, CallbackCommand.class);

        //修改log状态
        Map map = new HashMap<>();
        map.put("id", callbackCommand.getJobLogId());
        map.put("callbackMessage", callbackCommand.getHandleMsg());
        map.put("callbackTime", callbackCommand.getHandleTime());
        map.put("callbackStatus", callbackCommand.getHandleCode());
        scheduleJobLogDao.updateCallback(map);
        PlatformRemotingCommand response = new PlatformRemotingCommand();
        return response;
    }

    @Override
    public boolean rejectRequest() {
        return false;
    }

}
