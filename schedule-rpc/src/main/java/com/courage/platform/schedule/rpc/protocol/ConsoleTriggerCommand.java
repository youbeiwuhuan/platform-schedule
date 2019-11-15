package com.courage.platform.schedule.rpc.protocol;

/**
 * 控制台发送命令 到server 来控制任务
 * Created by zhangyong on 2019/11/12.
 */
public class ConsoleTriggerCommand extends BaseCommand {

    private Long jobId;

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

}
