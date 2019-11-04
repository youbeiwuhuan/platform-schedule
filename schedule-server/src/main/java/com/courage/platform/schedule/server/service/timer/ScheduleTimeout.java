package com.courage.platform.schedule.server.service.timer;


public interface ScheduleTimeout {

    ScheduleTimer timer();

    ScheduleTimerTask task();

    boolean isExpired();

    boolean isCancelled();

    boolean cancel();

}
