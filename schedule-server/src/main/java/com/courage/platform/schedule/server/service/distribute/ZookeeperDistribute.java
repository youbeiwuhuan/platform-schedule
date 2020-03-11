package com.courage.platform.schedule.server.service.distribute;

import com.courage.platform.schedule.core.util.IpUtil;
import com.courage.platform.schedule.dao.domain.PlatformNamesrv;
import com.courage.platform.schedule.rpc.ScheduleRpcServer;
import com.courage.platform.schedule.server.service.PlatformNamesrvService;
import com.courage.platform.schedule.server.service.ScheduleJobExecutor;
import com.courage.platform.schedule.server.service.ScheduleJobInfoService;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
        //核心思路是: 在zk /platform/schedule/servers 节点添加 127.0.0.1
        //在/platform/schedule/servers/leader 节点下添加 若leader目录下存在节点 则监听节点 ，当前server状态是standby 若无节点，则做为leader ，作为任务分配的服务器
        long start = System.currentTimeMillis();
        logger.warn("开始启动zk任务分发器");
        boolean hostAvailable = isCurrentHost();
        logger.warn("结束启动zk任务分发器,耗时:" + (System.currentTimeMillis() - start) + "ms");
    }

    private boolean isCurrentHost() {
        String currentHost = IpUtil.getIpPort(scheduleRpcServer.localListenPort());
        List<PlatformNamesrv> platformNamesrvList = platformNamesrvService.findAll();
        if (CollectionUtils.isNotEmpty(platformNamesrvList)) {
            for (PlatformNamesrv platformNamesrv : platformNamesrvList) {
                if (currentHost.equals(platformNamesrv.getNamesrvIp())) {
                    return true;
                }
            }
        }
        logger.warn("currentHost:" + currentHost + " isnot available , you should deploy to another container!");
        return false;
    }

    @Override
    public void shutdown() {

    }

}
