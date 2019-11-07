package com.courage.platform.schedule.server.service;

import com.courage.platform.schedule.core.util.IdGenerator;
import com.courage.platform.schedule.core.util.IpUtil;
import com.courage.platform.schedule.dao.ScheduleJobLogDao;
import com.courage.platform.schedule.dao.domain.ScheduleJobInfo;
import com.courage.platform.schedule.dao.domain.ScheduleJobLog;
import com.courage.platform.schedule.dao.domain.ScheduleLogStatusEnum;
import com.courage.platform.schedule.rpc.ScheduleRpcServer;
import com.courage.platform.schedule.server.rpc.RpcChannelManager;
import com.courage.platform.schedule.server.rpc.RpcChannelSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * rpc调度服务
 * Created by zhangyong on 2019/11/4.
 */
@Service
public class ScheduleTriggerService {

    private final static Logger logger = LoggerFactory.getLogger(ScheduleTriggerService.class);

    private final static int workerId = Math.abs(IpUtil.getIp().hashCode() % 1024);

    @Autowired
    private ScheduleJobLogDao scheduleJobLogDao;

    @Autowired
    private ScheduleJobInfoService scheduleJobInfoService;

    @Autowired
    private ScheduleRpcServer scheduleRpcServer;

    @Autowired
    private RpcChannelManager rpcChannelManager;

    public void doRpcTrigger(Long jobId) {
        try {
            ScheduleJobInfo scheduleJobInfo = scheduleJobInfoService.getById(jobId);
            scheduleJobInfo.setTriggerLastTime(new Date());
            //存储到db
            ScheduleJobLog scheduleJobLog = new ScheduleJobLog();
            Long id = IdGenerator.getUniqueIdAutoSeq(workerId);
            scheduleJobLog.setId(id);
            scheduleJobLog.setJobId(scheduleJobInfo.getId());
            scheduleJobLog.setAppId(scheduleJobInfo.getAppId());
            scheduleJobLog.setCreateTime(new Date());
            scheduleJobLog.setTriggerTime(new Date());
            scheduleJobLog.setStatus(ScheduleLogStatusEnum.INITIALIZE.getId());
            scheduleJobLogDao.insert(scheduleJobLog);

            logger.info("开始执行:" + scheduleJobInfo.getJobName() + " param:" + scheduleJobInfo.getJobParam());
            //获取当前存在的链接
            List<RpcChannelSession> rpcChannelSessionList = rpcChannelManager.getChannelSessionListByAppName(scheduleJobInfo.getAppName());
            //scheduleRpcServer.getNodePlatformRemotingServer().invokeSync(null, null, 5000L);
        } catch (Exception e) {
            logger.error("doRpcTrigger error:", e);
        }
    }

}
