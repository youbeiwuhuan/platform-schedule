package com.courage.platform.schedule.client.rpc.controller;

import com.courage.platform.schedule.rpc.ScheduleRpcClient;
import com.courage.platform.schedule.rpc.protocol.CallbackCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScheduleClientController {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleClientController.class);
    
    private final static ScheduleRpcClient scheduleRpcClient = ScheduleRpcClient.getSingleInstance();

    private static ScheduleClientController instance = new ScheduleClientController();

    private ScheduleClientController() {
    }

    public static ScheduleClientController getSingleInstance() {
        return instance;
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

