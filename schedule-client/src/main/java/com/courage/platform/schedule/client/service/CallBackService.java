package com.courage.platform.schedule.client.service;

import com.alibaba.fastjson.JSON;
import com.courage.platform.schedule.client.common.ScheduleUtils;
import com.courage.platform.schedule.client.domain.ScheduleParam;
import com.courage.platform.schedule.client.domain.ScheduleResult;
import com.courage.platform.schedule.client.invoke.ClientInvoke;
import com.courage.platform.schedule.client.manager.PlatformScheduleResolver;
import com.courage.platform.schedule.rpc.ScheduleRpcClient;
import com.courage.platform.schedule.rpc.protocol.CallbackCommand;
import com.courage.platform.schedule.rpc.protocol.CommandEnum;
import com.courage.platform.schedule.rpc.protocol.TriggerCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Objects;

/**
 * Created by 王鑫 on 2018/10/19.
 */
public class CallBackService implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(CallBackService.class);

    private String remoteAddress;

    private TriggerCommand triggerCommand;

    private ScheduleRpcClient scheduleRpcClient;

    public CallBackService(ScheduleRpcClient scheduleRpcClient, String remoteAddress, TriggerCommand triggerCommand) {
        this.scheduleRpcClient = scheduleRpcClient;
        this.remoteAddress = remoteAddress;
        this.triggerCommand = triggerCommand;
    }

    @Override
    public void run() {
        int handleCode = 0;
        String handlerMsg = null;
        try {
            String serviceId = triggerCommand.getServiceId();
            ScheduleParam scheduleParam = new ScheduleParam();
            scheduleParam.setJobLogId(triggerCommand.getJobLogId());
            scheduleParam.setExecutorParam(triggerCommand.getExecutorParam());
            scheduleParam.setCreateMillisTime(triggerCommand.getCreateMillisTime());

            ClientInvoke clientInvoke = PlatformScheduleResolver.getInvoker(serviceId);
            Objects.requireNonNull(clientInvoke, "调度服务不存在");

            Object[] parms = {scheduleParam};
            //调用方法
            ScheduleResult scheduleResult = (ScheduleResult) clientInvoke.invoke(serviceId, parms);
            Objects.requireNonNull(scheduleResult, "调用失败，返回ScheduleResult为null");

            //任务真正执行结果
            handleCode = scheduleResult.getCode();
            handlerMsg = scheduleResult.getMsg();
        } catch (Exception e) {
            handlerMsg = "执行调度任务异常，调用IP：" + remoteAddress + ",请求命令" + JSON.toJSONString(triggerCommand);
            handleCode = ScheduleResult.FAIL_CODE;
            handlerMsg = handlerMsg + "\n" + ScheduleUtils.getlogMsg(e);
            logger.error(handlerMsg, e);
        }
        //发送日志信息
        sendScheduleServer(triggerCommand, handleCode, handlerMsg);
    }

    /**
     * 发送日志信息
     *
     * @param triggerCommand
     * @param handleCode
     * @param handlerMsg
     */
    private void sendScheduleServer(TriggerCommand triggerCommand, int handleCode, String handlerMsg) {
        try {
            //发送记录日志
            CallbackCommand callbackCommand = new CallbackCommand();
            callbackCommand.setJobId(triggerCommand.getJobId());
            callbackCommand.setJobLogId(triggerCommand.getJobLogId());
            callbackCommand.setHandleTime(new Date());
            callbackCommand.setHandleCode(String.valueOf(handleCode));
            callbackCommand.setHandleMsg(handlerMsg);

            scheduleRpcClient.send(remoteAddress, CommandEnum.CALLBACK_SCHEDULE_RESULT_CMD, callbackCommand);
        } catch (Throwable e) {
            logger.error("发送日志信息异常", e);
        }
    }


}
