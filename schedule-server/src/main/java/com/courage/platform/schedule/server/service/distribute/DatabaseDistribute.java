package com.courage.platform.schedule.server.service.distribute;

import com.courage.platform.schedule.core.util.IpUtil;
import com.courage.platform.schedule.dao.domain.PlatformNamesrv;
import com.courage.platform.schedule.dao.domain.ScheduleJobInfo;
import com.courage.platform.schedule.rpc.ScheduleRpcServer;
import com.courage.platform.schedule.server.service.PlatformNamesrvService;
import com.courage.platform.schedule.server.service.ScheduleJobExecutor;
import com.courage.platform.schedule.server.service.ScheduleJobInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.Map;

/**
 * 数据库 主从模式
 * Created by zhangyong on 2019/11/4.
 */
@Service
public class DatabaseDistribute implements TriggerMode {

    private final static Logger logger = LoggerFactory.getLogger(DatabaseDistribute.class);

    @Autowired
    private ScheduleJobInfoService scheduleJobInfoService;

    @Autowired
    private PlatformNamesrvService platformNamesrvService;

    @Autowired
    private ScheduleJobExecutor scheduleJobExecutor;

    @Autowired
    private ScheduleRpcServer scheduleRpcServer;

    @Override
    @Scheduled(initialDelay = 60000, fixedRate = 30000)
    public void start() {
        //检测当前是否有执行权限
        boolean isCurrentHostMasterRole = isCurrentHostMasterRole();
        if (isCurrentHostMasterRole) {
            Map<Long, ScheduleJobInfo> jobInfoMap = scheduleJobInfoService.getJobInfoCache();
            Iterator<Map.Entry<Long, ScheduleJobInfo>> entries = jobInfoMap.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<Long, ScheduleJobInfo> entry = entries.next();
                ScheduleJobInfo scheduleJobInfo = entry.getValue();
                scheduleJobExecutor.scheduleJob(scheduleJobInfo.getId());
            }
        } else {
            logger.info("删除内存中正在运行中的任务");
            scheduleJobExecutor.removeJobs();
        }
    }

    private boolean isCurrentHostMasterRole() {
        String currentHost = IpUtil.getIpPort(scheduleRpcServer.localListenPort());
        PlatformNamesrv platformNamesrv = platformNamesrvService.getPlatformNamesrvByNamesrvIp(currentHost);
        if (platformNamesrv == null || platformNamesrv.getRole() == 1) {
            logger.warn("当前host:" + currentHost + " 没有被设置为master role");
            return false;
        }
        logger.warn("当前host:" + currentHost + " 被设置为master role");
        return true;
    }

    @Override
    public void shutdown() {

    }

}
