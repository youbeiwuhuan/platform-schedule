package com.courage.platform.schedule.client.domain;

import java.io.Serializable;

/**
 * 任务执行结果
 */
public class ScheduleResult implements Serializable {

    public static final long serialVersionUID = 32L;

    public static final int SUCCESS_CODE = 0;

    public static final int FAIL_CODE = 1;

    private int code;

    private String msg;

    public ScheduleResult(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ScheduleResult(String msg) {
        this.code = SUCCESS_CODE;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

}
