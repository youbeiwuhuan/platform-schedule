package com.courage.platform.schedule.server.service;

import com.courage.platform.rpc.remoting.netty.protocol.PlatformRemotingCommand;
import com.courage.platform.schedule.core.domain.TriggerStatusEnum;
import com.courage.platform.schedule.core.util.IdGenerator;
import com.courage.platform.schedule.core.util.IpUtil;
import com.courage.platform.schedule.dao.ScheduleJobLogDao;
import com.courage.platform.schedule.dao.domain.ScheduleJobInfo;
import com.courage.platform.schedule.dao.domain.ScheduleJobLog;
import com.courage.platform.schedule.rpc.ScheduleRpcServer;
import com.courage.platform.schedule.rpc.protocol.CommandEnum;
import com.courage.platform.schedule.server.rpc.RpcChannelManager;
import com.courage.platform.schedule.server.rpc.RpcChannelSession;
import org.apache.commons.collections4.CollectionUtils;
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
            logger.info("执行任务:" + scheduleJobInfo.getJobName() + " param:" + scheduleJobInfo.getJobParam());
            //存储到db
            ScheduleJobLog scheduleJobLog = new ScheduleJobLog();
            Long id = IdGenerator.getUniqueIdAutoSeq(workerId);
            scheduleJobLog.setId(id);
            scheduleJobLog.setJobId(scheduleJobInfo.getId());
            scheduleJobLog.setAppId(scheduleJobInfo.getAppId());
            scheduleJobLog.setCreateTime(new Date());
            scheduleJobLog.setTriggerTime(new Date());
            scheduleJobLog.setTriggerStatus(TriggerStatusEnum.INITIALIZE.getId());

            List<RpcChannelSession> rpcChannelSessionList = rpcChannelManager.getChannelSessionListByAppName(scheduleJobInfo.getAppName());
            if (CollectionUtils.isNotEmpty(rpcChannelSessionList)) {
                RpcChannelSession rpcChannelSession = rpcChannelSessionList.get(0);
                PlatformRemotingCommand platformRemotingCommand = new PlatformRemotingCommand();
                platformRemotingCommand.setRequestCmd(CommandEnum.TRIGGER_SCHEDULE_TASK_CMD);
                scheduleRpcServer.getNodePlatformRemotingServer().invokeOneway(rpcChannelSession.getChannel(), platformRemotingCommand, 5000L);
                scheduleJobLog.setTriggerStatus(TriggerStatusEnum.SUCCESS.getId());
            } else {
                scheduleJobLog.setTriggerStatus(TriggerStatusEnum.FAIL.getId());
                scheduleJobLog.setMessage("应用没有链接到调度服务中心");
            }
            scheduleJobLogDao.insert(scheduleJobLog);
        } catch (Exception e) {
            logger.error("doRpcTrigger error:", e);
        }
    }

}
