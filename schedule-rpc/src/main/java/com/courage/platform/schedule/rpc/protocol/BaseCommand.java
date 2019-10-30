package com.courage.platform.schedule.rpc.protocol;

/**
 * 调度任务基础命令类
 * Created by zhangyong on 2018/10/5.
 */
public abstract class BaseCommand {

    //任务Id
    private Long jobId;

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

}
