package com.courage.platform.schedule.server.service.distribute;

import com.courage.platform.schedule.rpc.ScheduleRpcServer;
import com.courage.platform.schedule.server.service.PlatformNamesrvService;
import com.courage.platform.schedule.server.service.ScheduleJobExecutor;
import com.courage.platform.schedule.server.service.ScheduleJobInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ZookeeperDistribute implements DistributeMode {

    private final static Logger logger = LoggerFactory.getLogger(ZookeeperDistribute.class);

    @Autowired
    private ScheduleJobInfoService scheduleJobInfoService;

    @Autowired
    private PlatformNamesrvService platformNamesrvService;

    @Autowired
    private ScheduleJobExecutor scheduleJobExecutor;

    @Autowired
    private ScheduleRpcServer scheduleRpcServer;

    @Override
    public void start() {
        long start = System.currentTimeMillis();
        logger.warn("开始启动zk任务分发器");
        logger.warn("结束启动zk任务分发器,耗时:" + (System.currentTimeMillis() - start) + "ms");
    }

    @Override
    public void shutdown() {

    }

}
