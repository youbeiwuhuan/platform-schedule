package com.courage.platform.schedule.rpc.protocol;

/**
 * 控制台
 * Created by zhangyong on 2019/11/15.
 */
public class ConsoleOnlineAppCommand extends BaseCommand {

    private String namesrvIp;

    private int start;

    private int pageSize;

    public String getNamesrvIp() {
        return namesrvIp;
    }

    public void setNamesrvIp(String namesrvIp) {
        this.namesrvIp = namesrvIp;
    }

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

}
