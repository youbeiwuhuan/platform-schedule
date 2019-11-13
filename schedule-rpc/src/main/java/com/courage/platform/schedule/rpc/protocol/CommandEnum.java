package com.courage.platform.schedule.rpc.protocol;

/**
 * 任务调度请求命令
 */
public class CommandEnum {

    //触发调度任务 (服务端发起)
    public static final int TRIGGER_SCHEDULE_TASK_CMD = 10000;


    //反馈任务执行结果 (客户端发起)
    public static final int CALLBACK_SCHEDULE_RESULT_CMD = 10001;

    //callbackScheduleResult

    //查询任务调用日志 (服务端发起)
    public static final int QUERY_SCHEDULE_LOG_CMD = 10002;

    //注册命令
    public static final int REGISTER_CMD = 10003;

    //触发调度任务（客户端发起）
    public static final int CONSOLE_TRIGGER_SCHEDULE_TASK_CMD = 10004;

}
