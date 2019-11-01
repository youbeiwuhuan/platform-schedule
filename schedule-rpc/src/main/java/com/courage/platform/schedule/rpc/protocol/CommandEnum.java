package com.courage.platform.schedule.rpc.protocol;

/**
 * 任务调度请求命令
 */
public class CommandEnum {

    //触发调度任务
    public static final int TRIGGER_SCHEDULE_TASK_CMD = 10000;

    //反馈任务执行结果
    public static final int CALLBACK_SCHEDULE_RESULT_CMD = 10001;

    //callbackScheduleResult

    //查询任务调用日志
    public static final int QUERY_SCHEDULE_LOG_CMD = 10002;

    //心跳命令(也相当于注册信息)
    public static final int SCHEDULE_HEART_BEAT_CMD = 10003;

}
