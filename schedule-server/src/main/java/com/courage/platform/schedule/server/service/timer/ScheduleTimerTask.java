package com.courage.platform.schedule.server.service.timer;

/**
 * Created by zhangyong on 2019/11/4.
 */
public interface ScheduleTimerTask {


    void run(ScheduleTimeout timeout) throws Exception;

}
