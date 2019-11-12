package com.courage.platform.schedule.rpc.protocol;

/**
 * client app 发送注册命令到 server端
 * Created by zhangyong on 2019/11/9.
 */
public class RegisterCommand extends BaseCommand {

    private String appName;

    private String clientId;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

}

