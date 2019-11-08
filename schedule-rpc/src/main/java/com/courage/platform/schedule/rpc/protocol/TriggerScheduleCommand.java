package com.courage.platform.schedule.rpc.protocol;

/**
 * 触发任务调度命令
 * Created by zhangyong on 2018/10/5.
 */
public class TriggerScheduleCommand extends BaseCommand {

    //任务Id
    private Long jobId;
    /**
     * 服务ID
     */
    private String serviceId;

    /**
     * 任务执行参数
     */
    private String executorParam;

    /**
     * 调度任务日志id
     */
    private String jobLogId;

    /**
     * 任务调用创建时间
     */
    private long createMillisTime;

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getExecutorParam() {
        return executorParam;
    }

    public void setExecutorParam(String executorParam) {
        this.executorParam = executorParam;
    }

    public String getJobLogId() {
        return jobLogId;
    }

    public void setJobLogId(String jobLogId) {
        this.jobLogId = jobLogId;
    }

    public long getCreateMillisTime() {
        return createMillisTime;
    }

    public void setCreateMillisTime(long createMillisTime) {
        this.createMillisTime = createMillisTime;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

}
