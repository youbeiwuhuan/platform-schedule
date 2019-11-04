package com.courage.platform.schedule.server.service.timer;

import io.netty.util.TimerTask;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhangyong on 2019/11/4.
 */
public interface ScheduleTimer {

    /**
     * Schedules the specified {@link ScheduleTimerTask} for one-time execution after
     * the specified delay.
     *
     * @return a handle which is associated with the specified task
     * @throws IllegalStateException if this timer has been
     *                               {@linkplain #stop() stopped} already
     */
    ScheduleTimeout newTimeout(ScheduleTimerTask task, long delay, TimeUnit unit);

    /**
     * Releases all resources acquired by this {@link io.netty.util.Timer} and cancels all
     * tasks which were scheduled but not executed yet.
     *
     * @return the handles associated with the tasks which were canceled by
     * this method
     */
    Set<ScheduleTimeout> stop();

}
