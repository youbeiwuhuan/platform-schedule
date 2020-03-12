package com.courage.platform.schedule.server.service.distribute;

import com.courage.platform.schedule.dao.domain.ScheduleJobInfo;
import com.courage.platform.schedule.server.service.PlatformNamesrvService;
import com.courage.platform.schedule.server.service.ScheduleJobExecutor;
import com.courage.platform.schedule.server.service.ScheduleJobInfoService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Iterator;
import java.util.Map;

/**
 * 数据库 主从模式
 * Created by zhangyong on 2019/11/4.
 */
public class DatabaseDistribute implements DistributeMode {

    @Autowired
    private ScheduleJobInfoService scheduleJobInfoService;

    @Autowired
    private PlatformNamesrvService platformNamesrvService;

    @Autowired
    private ScheduleJobExecutor scheduleJobExecutor;

    @Override
    public void start() {
        Map<Long, ScheduleJobInfo> jobInfoMap = scheduleJobInfoService.getJobInfoCache();
        Iterator<Map.Entry<Long, ScheduleJobInfo>> entries = jobInfoMap.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<Long, ScheduleJobInfo> entry = entries.next();
            ScheduleJobInfo scheduleJobInfo = entry.getValue();
            scheduleJobExecutor.scheduleJob(scheduleJobInfo.getId());
        }
    }

    @Override
    public void shutdown() {

    }

}
