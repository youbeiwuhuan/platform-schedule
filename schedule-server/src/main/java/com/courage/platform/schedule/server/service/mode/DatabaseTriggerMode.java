package com.courage.platform.schedule.server.service.mode;

import com.courage.platform.schedule.core.util.IpUtil;
import com.courage.platform.schedule.dao.domain.PlatformNamesrv;
import com.courage.platform.schedule.dao.domain.ScheduleJobInfo;
import com.courage.platform.schedule.rpc.config.ScheduleRpcServerConfig;
import com.courage.platform.schedule.server.service.PlatformNamesrvService;
import com.courage.platform.schedule.server.service.ScheduleJobExecutor;
import com.courage.platform.schedule.server.service.ScheduleJobInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.Map;

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

    @Autowired
    private ScheduleJobExecutor scheduleJobExecutor;

    @Override
    public void start() {
        //检测当前是否有执行权限
        boolean isCurrentHostMasterRole = isCurrentHostMasterRole();
        if (!isCurrentHostMasterRole) {
            return;
        }
        Map<Long, ScheduleJobInfo> jobInfoMap = scheduleJobInfoService.getJobInfoCache();
        Iterator<Map.Entry<Long, ScheduleJobInfo>> entries = jobInfoMap.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<Long, ScheduleJobInfo> entry = entries.next();
            ScheduleJobInfo scheduleJobInfo = entry.getValue();
            scheduleJobExecutor.addJob(scheduleJobInfo.getId());
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

    @Override
    public void shutdown() {

    }

}
