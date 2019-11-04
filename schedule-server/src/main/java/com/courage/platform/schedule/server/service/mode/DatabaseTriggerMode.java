package com.courage.platform.schedule.server.service.mode;

import com.courage.platform.schedule.core.util.IpUtil;
import com.courage.platform.schedule.dao.domain.PlatformNamesrv;
import com.courage.platform.schedule.rpc.config.ScheduleRpcServerConfig;
import com.courage.platform.schedule.server.service.PlatformNamesrvService;
import com.courage.platform.schedule.server.service.ScheduleJobInfoService;
import com.courage.platform.schedule.server.service.timer.ScheduleHashedWheelTimer;
import com.courage.platform.schedule.server.service.timer.ScheduleTimeout;
import com.courage.platform.schedule.server.service.timer.ScheduleTimerTask;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 数据库 主从模式
 * Created by zhangyong on 2019/11/4.
 */
@Service
public class DatabaseTriggerMode implements TriggerMode {

    private final static Logger logger = LoggerFactory.getLogger(DatabaseTriggerMode.class);

    @Autowired
    private ScheduleJobInfoService scheduleJobInfoService;

    @Autowired
    private PlatformNamesrvService platformNamesrvService;

    @Override
    public void start() {
        boolean isCurrentHostMasterRole = isCurrentHostMasterRole();
        ScheduleHashedWheelTimer scheduleHashedWheelTimer = new ScheduleHashedWheelTimer();
        scheduleHashedWheelTimer.newTimeout(new ScheduleTimerTask() {
            @Override
            public void run(ScheduleTimeout timeout) throws Exception {
                String date = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
                System.out.println(date + "doing same thing");
            }
        }, 5, TimeUnit.SECONDS);
        scheduleHashedWheelTimer.start();

        scheduleHashedWheelTimer.newTimeout(new ScheduleTimerTask() {
            @Override
            public void run(ScheduleTimeout timeout) throws Exception {
                String date = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
                System.out.println(date + "doing other thing");
            }
        }, 20, TimeUnit.SECONDS);
    }

    @Override
    public void shutdown() {

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
