package com.courage.platform.schedule.server.service.mode;

import com.courage.platform.schedule.core.cron.CronExpression;
import com.courage.platform.schedule.core.util.IpUtil;
import com.courage.platform.schedule.core.util.ThreadFactoryImpl;
import com.courage.platform.schedule.dao.domain.PlatformNamesrv;
import com.courage.platform.schedule.dao.domain.ScheduleJobInfo;
import com.courage.platform.schedule.rpc.config.ScheduleRpcServerConfig;
import com.courage.platform.schedule.server.service.PlatformNamesrvService;
import com.courage.platform.schedule.server.service.ScheduleJobInfoService;
import com.courage.platform.schedule.server.service.timer.ScheduleHashedWheelTimer;
import com.courage.platform.schedule.server.service.timer.ScheduleTimeout;
import com.courage.platform.schedule.server.service.timer.ScheduleTimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 数据库 主从模式
 * Created by zhangyong on 2019/11/4.
 */
@Service
public class DatabaseTriggerMode implements TriggerMode {

    private final static Logger logger = LoggerFactory.getLogger(DatabaseTriggerMode.class);

    private final static ThreadPoolExecutor notifyExecutor = new ThreadPoolExecutor(32, 64, 1000 * 60, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactoryImpl("JobExecuteThread_"));

    @Autowired
    private ScheduleJobInfoService scheduleJobInfoService;

    @Autowired
    private PlatformNamesrvService platformNamesrvService;


    private ScheduleHashedWheelTimer scheduleHashedWheelTimer;

    @Override
    public void start() {
        //检测当前是否有执行权限
        boolean isCurrentHostMasterRole = isCurrentHostMasterRole();
        if (!isCurrentHostMasterRole) {
            return;
        }

        Date currentDate = new Date();
        scheduleHashedWheelTimer = new ScheduleHashedWheelTimer(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "HashedWheelTimer");
            }
        });

        Map<Long, ScheduleJobInfo> jobInfoMap = scheduleJobInfoService.getJobInfoCache();
        Iterator<Map.Entry<Long, ScheduleJobInfo>> entries = jobInfoMap.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<Long, ScheduleJobInfo> entry = entries.next();
            Long jobId = entry.getKey();
            ScheduleJobInfo scheduleJobInfo = entry.getValue();
            Date nextExecuteDate = null;
            try {
                CronExpression cronExpression = new CronExpression(scheduleJobInfo.getJobCron());
                nextExecuteDate = cronExpression.getNextValidTimeAfter(currentDate);
            } catch (Exception e) {
                logger.error("parse scheduleJobInfo error:", e);
            }
            if (nextExecuteDate != null) {
                long delay = nextExecuteDate.getTime() - currentDate.getTime();
                scheduleHashedWheelTimer.newTimeout(new ScheduleTimerTask() {
                    @Override
                    public void run(ScheduleTimeout timeout) throws Exception {
                        //发送rpc 请求到channel
                        logger.info("running" + scheduleJobInfo.getJobName());
                    }
                }, delay, TimeUnit.MILLISECONDS);
            }
        }
        scheduleHashedWheelTimer.start();
    }

    @Override
    public void shutdown() {
        if (scheduleHashedWheelTimer != null) {
            scheduleHashedWheelTimer.stop();
        }
    }

    private boolean isCurrentHostMasterRole() {
        String currentHost = IpUtil.getIpPort(ScheduleRpcServerConfig.TASK_PRC_LISTEN_PORT);
        PlatformNamesrv platformNamesrv = platformNamesrvService.getPlatformNamesrvByNamesrvIp(currentHost);
        if (platformNamesrv == null || platformNamesrv.getRole() == 1) {
            logger.warn("当前host:" + currentHost + " 没有被设置为master role");
            return false;
        }
        logger.warn("当前host:" + currentHost + " 被设置为master role");
        return true;
    }

}
