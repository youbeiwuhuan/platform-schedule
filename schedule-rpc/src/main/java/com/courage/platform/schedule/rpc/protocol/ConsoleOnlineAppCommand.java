package com.courage.platform.schedule.rpc.protocol;

/**
 * 控制台
 * Created by zhangyong on 2019/11/15.
 */
public class ConsoleOnlineAppCommand extends BaseCommand {

    private String appName = "";

    private int start = 0;

    private int pageSize = 10;

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

}
