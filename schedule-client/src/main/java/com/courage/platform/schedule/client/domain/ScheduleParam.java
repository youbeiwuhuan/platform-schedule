package com.courage.platform.schedule.client.domain;

import java.io.Serializable;

/**
 * 任务执行参数
 */
public class ScheduleParam implements Serializable {

    public static final long serialVersionUID = 42L;

    //任务执行参数
    private String executorParam;

    //调度任务日志id
    private String jobLogId;

    //任务调用创建时间
    private long createMillisTime;

    public String getExecutorParam() {
        return executorParam;
    }

    public void setExecutorParam(String executorParam) {
        this.executorParam = executorParam;
    }

    public long getCreateMillisTime() {
        return createMillisTime;
    }

    public void setCreateMillisTime(long createMillisTime) {
        this.createMillisTime = createMillisTime;
    }

    public String getJobLogId() {
        return jobLogId;
    }

    public void setJobLogId(String jobLogId) {
        this.jobLogId = jobLogId;
    }

}
