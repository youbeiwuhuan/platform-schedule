package com.courage.platform.schedule.client.manager;


import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlatformSchedulerClient {

    private final static Logger logger = LoggerFactory.getLogger(PlatformSchedulerClient.class);

    private String appName;

    private String appKey;

    private String consoleAddress;

    public void start() {
        if (StringUtils.isNotBlank(appName) || StringUtils.isNotBlank(consoleAddress)) {
            logger.error("任务调度服务需要配置appName & consoleAddress");
            return;
        }
    }

    public void destroy() {

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

