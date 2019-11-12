package com.courage.platform.schedule.rpc.protocol;

/**
 * Created by zhangyong on 2019/11/12.
 */
public class OtherTriggerCommand extends BaseCommand {

    private Long jobId;


    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

}
