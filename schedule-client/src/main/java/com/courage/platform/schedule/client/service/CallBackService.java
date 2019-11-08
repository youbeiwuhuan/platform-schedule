package com.courage.platform.schedule.client.service;

import com.alibaba.fastjson.JSON;
import com.courage.platform.schedule.client.common.ScheduleUtils;
import com.courage.platform.schedule.client.domain.ScheduleParam;
import com.courage.platform.schedule.client.domain.ScheduleResult;
import com.courage.platform.schedule.client.invoke.ClientInvoke;
import com.courage.platform.schedule.client.manager.PlatformScheduleResolver;
import com.courage.platform.schedule.client.rpc.controller.ScheduleClientController;
import com.courage.platform.schedule.rpc.protocol.CallbackCommand;
import com.courage.platform.schedule.rpc.protocol.TriggerScheduleCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Objects;

/**
 * Created by 王鑫 on 2018/10/19.
 */
public class CallBackService implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(CallBackService.class);

    private final static ScheduleClientController scheduleClientController = ScheduleClientController.getSingleInstance();

    private String remoteAddress;

    private TriggerScheduleCommand triggerScheduleCommand;

    public CallBackService(String remoteAddress, TriggerScheduleCommand triggerScheduleCommand) {
        this.remoteAddress = remoteAddress;
        this.triggerScheduleCommand = triggerScheduleCommand;
    }


    @Override
    public void run() {
        int handleCode = 0;
        String handlerMsg = null;
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
            ScheduleResult scheduleResult = (ScheduleResult) clientInvoke.invoke(serviceId, parms);
            Objects.requireNonNull(scheduleResult, "调用失败，返回ScheduleResult为null");

            //任务真正执行结果
            handleCode = scheduleResult.getCode();
            handlerMsg = scheduleResult.getMsg();
        } catch (Exception e) {
            handlerMsg = "执行调度任务异常，调用IP：" + remoteAddress + ",请求命令" + JSON.toJSONString(triggerScheduleCommand);
            handleCode = ScheduleResult.FAIL_CODE;
            handlerMsg = handlerMsg + "\n" + ScheduleUtils.getlogMsg(e);
            logger.error(handlerMsg, e);
        }
        //发送日志信息
        sendScheduleServer(triggerScheduleCommand, handleCode, handlerMsg);
    }

    /**
     * 发送日志信息
     *
     * @param triggerScheduleCommand
     * @param handleCode
     * @param handlerMsg
     */
    private void sendScheduleServer(TriggerScheduleCommand triggerScheduleCommand, int handleCode, String handlerMsg) {
        try {
            //发送记录日志
            CallbackCommand callbackCommand = new CallbackCommand();
            callbackCommand.setJobId(triggerScheduleCommand.getJobId());
            callbackCommand.setJobLogId(triggerScheduleCommand.getJobLogId());
            callbackCommand.setHandleTime(new Date());
            callbackCommand.setHandleCode(String.valueOf(handleCode));
            callbackCommand.setHandleMsg(handlerMsg);

            scheduleClientController.request(callbackCommand);
        } catch (Throwable e) {
            logger.error("发送日志信息异常", e);
        }
    }


}
