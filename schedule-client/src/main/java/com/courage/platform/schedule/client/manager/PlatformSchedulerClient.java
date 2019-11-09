package com.courage.platform.schedule.client.manager;


import com.courage.platform.schedule.client.rpc.controller.ScheduleClientController;
import com.courage.platform.schedule.core.util.HttpClientUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

public class PlatformSchedulerClient {

    private final static Logger logger = LoggerFactory.getLogger(PlatformSchedulerClient.class);

    private ScheduleClientController scheduleClientController;

    private Timer timer;

    private String appName;

    private String appKey;

    private String consoleAddress;

    public void start() {
        if (StringUtils.isNotBlank(appName) || StringUtils.isNotBlank(consoleAddress)) {
            logger.error("任务调度服务需要配置appName & consoleAddress");
            return;
        }
        this.scheduleClientController = new ScheduleClientController();
        this.scheduleClientController.start();
        //定时从console抓取 schedule server地址 并定时注册到scheduleserver上
        this.timer = new Timer("ScheduleRegisterTimerThread", true);
        this.timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (StringUtils.isEmpty(consoleAddress)) {
                        logger.error("任务调度控制台地址为空");
                        return;
                    }
                    String rtn = HttpClientUtils.doGet(consoleAddress, null);
                    if (StringUtils.isBlank(rtn)) {
                        logger.error("无法从console控制台" + consoleAddress + " 中获取schedulerserver地址信息");
                        return;
                    }

                } catch (Throwable e) {
                    logger.error("scheduleAtFixedRate flush exception", e);
                }
            }
        }, 10000, 30000);
    }

    public void destroy() {
        if (this.scheduleClientController != null) {
            this.scheduleClientController.close();
        }
    }

    //======================================================================  set method   ======================================================
    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public void setConsoleAddress(String consoleAddress) {
        this.consoleAddress = consoleAddress;
    }

}

