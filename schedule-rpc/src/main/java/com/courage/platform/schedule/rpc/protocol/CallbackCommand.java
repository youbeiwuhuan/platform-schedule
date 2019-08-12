package com.courage.platform.schedule.rpc.protocol;

import java.util.Date;

/**
 * 回调任务执行结果
 */
public class CallbackCommand extends BaseCommand {

    /**
     * 当前任务执行的唯一日志标识
     */
    private String jobLogId;

    /**
     * 任务执行结果代码
     */
    private String handleCode;

    /**
     * 任务执行结果信息
     */
    private String handleMsg;

    /**
     * 任务执行完成的时刻
     */
    private Date handleTime;

    public String getHandleCode() {
        return handleCode;
    }

    public void setHandleCode(String handleCode) {
        this.handleCode = handleCode;
    }

    public String getHandleMsg() {
        return handleMsg;
    }

    public void setHandleMsg(String handleMsg) {
        this.handleMsg = handleMsg;
    }

    public Date getHandleTime() {
        return handleTime;
    }

    public void setHandleTime(Date handleTime) {
        this.handleTime = handleTime;
    }

    public String getJobLogId() {
        return jobLogId;
    }

    public void setJobLogId(String jobLogId) {
        this.jobLogId = jobLogId;
    }
}
