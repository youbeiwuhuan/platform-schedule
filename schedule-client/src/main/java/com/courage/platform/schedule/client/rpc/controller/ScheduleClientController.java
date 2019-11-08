package com.courage.platform.schedule.client.rpc.controller;

import com.courage.platform.schedule.rpc.ScheduleRpcClient;
import com.courage.platform.schedule.rpc.protocol.CallbackCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScheduleClientController {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleClientController.class);

    private ScheduleRpcClient scheduleRpcClient;

    private ScheduleClientController instance = new ScheduleClientController();

    private ScheduleClientController() {

    }

    public void request(CallbackCommand callbackCommand) {

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
}

