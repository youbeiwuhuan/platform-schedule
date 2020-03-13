package com.courage.platform.schedule.server.service.distribute;

import com.courage.platform.schedule.common.zookeeper.ZkClientx;
import com.courage.platform.schedule.common.zookeeper.ZookeeperPathUtils;
import com.courage.platform.schedule.core.util.IpUtil;
import com.courage.platform.schedule.rpc.ScheduleRpcServer;
import com.courage.platform.schedule.server.service.PlatformNamesrvService;
import com.courage.platform.schedule.server.service.ScheduleJobExecutor;
import com.courage.platform.schedule.server.service.ScheduleJobInfoService;
import org.I0Itec.zkclient.IZkDataListener;
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

    @Autowired
    private ZkClientx zkClientx;

    //监控leader节点
    private Thread monitorThread;

    private volatile boolean running = false;

    @Override
    public void start() {
        this.running = true;
        //核心思路是: 在zk /platform/schedule/servers 节点添加 127.0.0.1:12999
        //在/platform/schedule/servers/leader 节点下添加 若leader目录下存在节点 则监听节点 ，当前server状态是standby 若无节点，则做为leader ，作为任务分配的服务器
        long start = System.currentTimeMillis();
        logger.warn("开始启动zk任务分发器");
        //监听leader节点 若leader节点无leader 则申请为leader
        this.monitorThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (running) {
                    try {
                        startZkService();
                    } catch (Throwable e) {
                        logger.error("start zk service:", e);
                    }
                    try {
                        Thread.sleep(10000);
                    } catch (Exception e) {
                    }
                }
            }
        });
        this.monitorThread.start();
        logger.warn("结束启动zk任务分发器,耗时:" + (System.currentTimeMillis() - start) + "ms");
    }

    private void startZkService() {
        // String hostIp = IpUtil.getIpPort(scheduleRpcServer.localListenPort());
        //创建持久化节点
        preparePersisitNode();
        //创建临时节点

        String hostPath = ZookeeperPathUtils.SCHEDULE_SERVER_NODE + ZookeeperPathUtils.ZOOKEEPER_SEPARATOR + IpUtil.getIpPort(scheduleRpcServer.localListenPort());
        //在server上配置相关信息
        if (!zkClientx.exists(hostPath)) {
            zkClientx.createEphemeral(hostPath, IpUtil.getIpPort(scheduleRpcServer.localListenPort()));
        }
    }

    private void preparePersisitNode() {
        //server永久节点
        if (!zkClientx.exists(ZookeeperPathUtils.SCHEDULE_SERVER_NODE)) {
            zkClientx.createPersistent(ZookeeperPathUtils.SCHEDULE_SERVER_NODE, true);
        }
        //leader永久节点
        if (!zkClientx.exists(ZookeeperPathUtils.SCHEDULE_LEADER_NODE)) {
            zkClientx.createPersistent(ZookeeperPathUtils.SCHEDULE_LEADER_NODE, true);
        }
    }


    @Override
    public void shutdown() {
        this.running = false;
        this.monitorThread = null;
    }

}
