package com.courage.platform.schedule.client.rpc.processor;

import com.alibaba.fastjson.JSON;
import com.courage.platform.rpc.remoting.netty.codec.PlatformNettyRequestProcessor;
import com.courage.platform.rpc.remoting.netty.protocol.PlatformRemotingCommand;
import com.courage.platform.rpc.remoting.netty.protocol.PlatformRemotingSerializable;
import com.courage.platform.rpc.remoting.netty.protocol.PlatformRemotingSysResponseCode;
import com.courage.platform.schedule.client.common.ScheduleUtils;
import com.courage.platform.schedule.client.domain.ScheduleParam;
import com.courage.platform.schedule.client.domain.ScheduleResult;
import com.courage.platform.schedule.client.invoke.ClientInvoke;
import com.courage.platform.schedule.client.manager.PlatformScheduleResolver;
import com.courage.platform.schedule.rpc.protocol.TriggerScheduleCommand;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * 触发任务处理器
 * Created by 王鑫 on 2018/10/12.
 */
public class TriggerTaskProcessor implements PlatformNettyRequestProcessor {

    private final static Logger logger = LoggerFactory.getLogger(TriggerTaskProcessor.class);

    @Override
    public PlatformRemotingCommand processRequest(ChannelHandlerContext channelHandlerContext, PlatformRemotingCommand platformRemotingCommand) throws Exception {
        String remoteAddress = channelHandlerContext.channel().remoteAddress().toString();
        PlatformRemotingCommand response = new PlatformRemotingCommand();
        response.setCode(PlatformRemotingSysResponseCode.SUCCESS);
        response.setRemark("触发成功");

        TriggerScheduleCommand triggerScheduleCommand = null;
        try {
            triggerScheduleCommand = PlatformRemotingSerializable.decode(platformRemotingCommand.getBody(), TriggerScheduleCommand.class);
            Objects.requireNonNull(triggerScheduleCommand, "解析PlatformRemotingCommand body属性数据异常");

            String serviceId = triggerScheduleCommand.getServiceId();
            Objects.requireNonNull(serviceId, "调度参数为错误，serviceId为null");
        } catch (Exception e) {
            String error = "触发任务失败，执行调度任务异常，调用IP：" + remoteAddress + ",请求命令:" + new String(platformRemotingCommand.getBody()) + ",具体原因：";
            response.setCode(PlatformRemotingSysResponseCode.SYSTEM_ERROR);
            response.setRemark(error + ScheduleUtils.getlogMsg(e));
            logger.error(error, e);
        }
        return response;
    }

    @Override
    public boolean rejectRequest() {
        return false;
    }

    private ScheduleResult invokeJob(TriggerScheduleCommand triggerScheduleCommand) {
        ScheduleResult scheduleResult;
        try {
            String serviceId = triggerScheduleCommand.getServiceId();
            ScheduleParam scheduleParam = new ScheduleParam();
            scheduleParam.setJobLogId(triggerScheduleCommand.getJobLogId());
            scheduleParam.setExecutorParam(triggerScheduleCommand.getExecutorParam());
            scheduleParam.setCreateMillisTime(triggerScheduleCommand.getCreateMillisTime());

            ClientInvoke clientInvoke = PlatformScheduleResolver.getInvoker(serviceId);
            Objects.requireNonNull(clientInvoke, "调度服务不存在");

            Object[] parms = {scheduleParam};
            //调用方法
            scheduleResult = (ScheduleResult) clientInvoke.invoke(serviceId, parms);
        } catch (Exception e) {
            String handlerMsg = "执行调度任务异常,请求命令" + JSON.toJSONString(triggerScheduleCommand);
            handlerMsg = handlerMsg + "\n" + ScheduleUtils.getlogMsg(e);
            scheduleResult = new ScheduleResult(ScheduleResult.FAIL_CODE, handlerMsg);
            logger.error(handlerMsg, e);
        }
        return scheduleResult;
    }

}
