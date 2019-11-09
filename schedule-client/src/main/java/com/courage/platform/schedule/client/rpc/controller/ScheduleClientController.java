package com.courage.platform.schedule.client.rpc.controller;

import com.courage.platform.schedule.client.rpc.processor.TriggerTaskProcessor;
import com.courage.platform.schedule.rpc.ScheduleRpcClient;
import com.courage.platform.schedule.rpc.protocol.CallbackCommand;
import com.courage.platform.schedule.rpc.protocol.CommandEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScheduleClientController {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleClientController.class);

    private ScheduleRpcClient scheduleRpcClient;

    public ScheduleClientController() {
        this.scheduleRpcClient = new ScheduleRpcClient();
        //server端推送需要处理
        this.scheduleRpcClient.registerProcessor(CommandEnum.TRIGGER_SCHEDULE_TASK_CMD, new TriggerTaskProcessor(this.scheduleRpcClient));
    }PlatformSchedulerClient

    public void start() {
        this.scheduleRpcClient.start();
    }

    public void close() {
        try {
            if (scheduleRpcClient != null) {
                scheduleRpcClient.shutdown();
            }
        } catch (Exception e) {
            logger.error("关闭platformNettyRemotingClient失败", e);
        }
    }

    public void request(CallbackCommand callbackCommand) {
        try {
            this.scheduleRpcClient.send(null, CommandEnum.CALLBACK_SCHEDULE_RESULT_CMD, callbackCommand);
        } catch (Throwable throwable) {
            logger.error("request callbackCommand error:", throwable);
        }
    }

}

