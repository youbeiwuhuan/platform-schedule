package com.courage.platform.schedule.server.service;

import com.alibaba.fastjson.JSON;
import com.courage.platform.rpc.remoting.netty.protocol.PlatformRemotingCommand;
import com.courage.platform.rpc.remoting.netty.protocol.PlatformRemotingSysResponseCode;
import com.courage.platform.schedule.core.domain.TriggerStatusEnum;
import com.courage.platform.schedule.core.util.IdGenerator;
import com.courage.platform.schedule.core.util.IpUtil;
import com.courage.platform.schedule.dao.ScheduleJobLogDao;
import com.courage.platform.schedule.dao.domain.ScheduleJobInfo;
import com.courage.platform.schedule.dao.domain.ScheduleJobLog;
import com.courage.platform.schedule.rpc.ScheduleRpcServer;
import com.courage.platform.schedule.rpc.protocol.CommandEnum;
import com.courage.platform.schedule.rpc.protocol.TriggerCommand;
import com.courage.platform.schedule.server.rpc.RpcChannelManager;
import com.courage.platform.schedule.server.rpc.RpcChannelSession;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
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
                Collections.shuffle(rpcChannelSessionList);
                RpcChannelSession rpcChannelSession = rpcChannelSessionList.get(0);
                PlatformRemotingCommand platformRemotingCommand = new PlatformRemotingCommand();
                platformRemotingCommand.setRequestCmd(CommandEnum.TRIGGER_SCHEDULE_TASK_CMD);
                TriggerCommand triggerCommand = new TriggerCommand();
                triggerCommand.setExecutorParam(StringUtils.trimToEmpty(scheduleJobInfo.getJobParam()));
                triggerCommand.setJobId(scheduleJobLog.getJobId());
                triggerCommand.setJobLogId(String.valueOf(id));
                triggerCommand.setServiceId(scheduleJobInfo.getJobHandler());
                platformRemotingCommand.setBody(JSON.toJSONBytes(triggerCommand));
                //同步调用发送给client客户端命令
                PlatformRemotingCommand response = scheduleRpcServer.getNodePlatformRemotingServer().invokeSync(rpcChannelSession.getChannel(), platformRemotingCommand, 5000L);
                if (response != null && response.getCode() == PlatformRemotingSysResponseCode.SUCCESS) {
                    scheduleJobLog.setTriggerStatus(TriggerStatusEnum.SUCCESS.getId());
                    logger.info("任务id:" + id + " 任务名:" + scheduleJobInfo.getJobName() + " 触发成功");
                } else {
                    scheduleJobLog.setTriggerStatus(TriggerStatusEnum.FAIL.getId());
                    String message = " 调用client失败,remoteAddr:" + rpcChannelSession.getChannel().remoteAddress().toString();
                    scheduleJobLog.setMessage(message);
                    logger.error("任务id:" + id + " 任务名:" + scheduleJobInfo.getJobName() + message);
                }
            } else {
                String message = " 应用没有链接到调度服务中心";
                scheduleJobLog.setTriggerStatus(TriggerStatusEnum.FAIL.getId());
                scheduleJobLog.setMessage(message);
                logger.warn("任务:" + scheduleJobInfo.getJobName() + message);
            }
            scheduleJobLogDao.insert(scheduleJobLog);
        } catch (Exception e) {
            logger.error("doRpcTrigger error:", e);
        }
    }

}
