package com.courage.platform.schedule.rpc.config;

/**
 * 任务调度服务端配置
 * Created by zhangyong on 2018/10/3.
 */
public class ScheduleRpcServerConfig {

    //监听端口
    public static final int TASK_PRC_LISTEN_PORT = 12999;

    //服务端链接删除时间 (1个小时)
    public static final int MAX_IDLE_TIME = 3600;

}
