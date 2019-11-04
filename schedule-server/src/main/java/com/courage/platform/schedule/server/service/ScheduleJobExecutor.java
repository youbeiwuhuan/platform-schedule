package com.courage.platform.schedule.server.service;

import com.alibaba.fastjson.JSON;
import com.courage.platform.schedule.core.cron.CronExpression;
import com.courage.platform.schedule.core.util.ThreadFactoryImpl;
import com.courage.platform.schedule.dao.domain.ScheduleJobInfo;
import com.courage.platform.schedule.server.service.timer.ScheduleHashedWheelTimer;
import com.courage.platform.schedule.server.service.timer.ScheduleTimeout;
import com.courage.platform.schedule.server.service.timer.ScheduleTimerTask;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 任务调度执行器
 * Created by zhangyong on 2019/11/4.
 */
@Service
public class ScheduleJobExecutor {

    private final static Logger logger = LoggerFactory.getLogger(ScheduleJobExecutor.class);

    private ThreadPoolExecutor executor = new ThreadPoolExecutor(32, 64, 1000 * 60, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactoryImpl("JobExecuteThread_"));

    private ScheduleHashedWheelTimer scheduleHashedWheelTimer;

    @Autowired
    private ScheduleRpcService scheduleRpcService;

    @PostConstruct
    public void start() {
        scheduleHashedWheelTimer = new ScheduleHashedWheelTimer(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "HashedWheelTimer");
            }
        });
        scheduleHashedWheelTimer.start();
    }

    public void addJob(ScheduleJobInfo scheduleJobInfo) {
        if (!NumberUtils.INTEGER_ZERO.equals(scheduleJobInfo.getStatus())) {
            logger.info("当前任务:" + JSON.toJSONString(scheduleJobInfo) + " 已经禁用!");
            return;
        }
        Date currentDate = new Date();
        Date nextExecuteDate = null;
        try {
            CronExpression cronExpression = new CronExpression(scheduleJobInfo.getJobCron());
            nextExecuteDate = cronExpression.getNextValidTimeAfter(currentDate);
        } catch (Exception e) {
            logger.error("parse scheduleJobInfo error:", e);
            return;
        }
        if (nextExecuteDate != null) {
            long delay = nextExecuteDate.getTime() - currentDate.getTime();
            scheduleJobInfo.setTriggerNextTime(nextExecuteDate);
            scheduleHashedWheelTimer.newTimeout(new ScheduleTimerTask() {
                @Override
                public void run(ScheduleTimeout timeout) throws Exception {
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            //调用rpc触发任务
                            scheduleRpcService.doRpcTrigger(scheduleJobInfo);
                            //计算下一次调度信息
                            addJob(scheduleJobInfo);
                        }
                    });
                }
            }, delay, TimeUnit.MILLISECONDS);
        }
    }

    @PreDestroy
    public void shutdown() {
        try {
            scheduleHashedWheelTimer.stop();
        } catch (Exception e) {
            //
        }
        try {
            executor.shutdown();
        } catch (Exception e) {
            logger.error("shutdown error:", e);
        }
    }

}
