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
import java.util.concurrent.*;

/**
 * 任务调度执行器
 * Created by zhangyong on 2019/11/4.
 */
@Service
public class ScheduleJobExecutor {

    private final static Logger logger = LoggerFactory.getLogger(ScheduleJobExecutor.class);

    private ThreadPoolExecutor executor = new ThreadPoolExecutor(32, 64, 1000 * 60, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactoryImpl("JobExecuteThread_"));

    private ConcurrentHashMap<Long, Integer> currentRunningJob = new ConcurrentHashMap<>(2048);

    private ScheduleHashedWheelTimer scheduleHashedWheelTimer;

    @Autowired
    private ScheduleRpcService scheduleRpcService;

    @Autowired
    private ScheduleJobInfoService scheduleJobInfoService;

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

    public synchronized void addJob(Long jobId) {
        if (currentRunningJob.containsKey(jobId)) {
            return;
        }
        currentRunningJob.put(jobId, JobAvailable.VALID.getId());
        logger.info(JSON.toJSONString(currentRunningJob));
        boolean result = executeJob(jobId);
        if (!result) {
            currentRunningJob.put(jobId, JobAvailable.UNVALID.getId());
        }
    }

    //该方法的前置条件是本地内存有标记
    public boolean executeJob(Long jobId) {
        ScheduleJobInfo scheduleJobInfo = scheduleJobInfoService.getById(jobId);
        if (scheduleJobInfo == null || !NumberUtils.INTEGER_ZERO.equals(scheduleJobInfo.getStatus())) {
            logger.info("当前任务:" + JSON.toJSONString(scheduleJobInfo) + " 已经禁用!");
            return false;
        }
        Date currentDate = new Date();
        Date nextExecuteDate = null;
        try {
            CronExpression cronExpression = new CronExpression(scheduleJobInfo.getJobCron());
            nextExecuteDate = cronExpression.getNextValidTimeAfter(currentDate);
        } catch (Exception e) {
            logger.error("parse scheduleJobInfo error:", e);
            logger.info("当前任务:" + JSON.toJSONString(scheduleJobInfo) + " cron表达式不合法!");
            return false;
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
                            try {
                                //调用rpc触发任务
                                scheduleRpcService.doRpcTrigger(scheduleJobInfo);
                            } catch (Throwable e) {
                                logger.error("doRpcTrigger error:", e);
                            }
                            //计算下一次调度信息
                            executeJob(jobId);
                        }
                    });
                }
            }, delay, TimeUnit.MILLISECONDS);
        }
        return true;
    }

    public void removeJobs() {
        currentRunningJob.clear();
    }

    public void removeJobById(Long jobId) {
        currentRunningJob.remove(jobId);
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

    private enum JobAvailable {

        UNVALID(0, "无效"), VALID(1, "有效");

        private Integer id;
        private String name;

        private JobAvailable(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

}
