package com.courage.platform.schedule.console.service;

import com.courage.platform.schedule.dao.ScheduleJobInfoDao;
import com.courage.platform.schedule.dao.domain.PlatformNamesrv;
import com.courage.platform.schedule.dao.domain.ScheduleJobInfo;
import com.courage.platform.schedule.rpc.ScheduleRpcClient;
import com.courage.platform.schedule.rpc.protocol.CommandEnum;
import com.courage.platform.schedule.rpc.protocol.ConsoleTriggerCommand;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangyong on 2019/11/5.
 */
@Service
public class ScheduleJobInfoService {

    private final static Logger logger = LoggerFactory.getLogger(ScheduleJobInfoService.class);

    @Autowired
    private ScheduleJobInfoDao scheduleJobInfoDao;

    @Autowired
    private PlatformNamesrvService platformNamesrvService;

    private ScheduleRpcClient scheduleRpcClient;

    @PostConstruct
    public void start() {
        this.scheduleRpcClient = new ScheduleRpcClient();
        this.scheduleRpcClient.start();
    }

    public List<ScheduleJobInfo> getPage(Map<String, Object> param, String start, Integer pageSize) {
        param.put("start", Integer.valueOf(start));
        param.put("pageSize", pageSize);
        return scheduleJobInfoDao.findPage(param);
    }

    public Integer count(Map<String, Object> param) {
        return scheduleJobInfoDao.count(param);
    }

    public Integer insert(Map<String, Object> param) {
        return scheduleJobInfoDao.insert(param);
    }

    public ScheduleJobInfo getById(String id) {
        return scheduleJobInfoDao.getById(id);
    }

    public void update(Map map) {
        scheduleJobInfoDao.update(map);
    }

    public void delete(Map map) {
        scheduleJobInfoDao.delete(map);
    }

    public boolean executeAtOnce(String jobId) {
        boolean flag = true;
        logger.info("立刻执行任务id:" + jobId);
        List<PlatformNamesrv> platformNamesrvList = platformNamesrvService.findAll();
        if (CollectionUtils.isNotEmpty(platformNamesrvList)) {
            ConsoleTriggerCommand consoleTriggerCommand = new ConsoleTriggerCommand();
            consoleTriggerCommand.setJobId(Long.valueOf(jobId));
            for (PlatformNamesrv platformNamesrv : platformNamesrvList) {
                //向master发送命令
                if (platformNamesrv.getRole().equals(0)) {
                    try {
                        scheduleRpcClient.send(platformNamesrv.getNamesrvIp(), CommandEnum.CONSOLE_TRIGGER_SCHEDULE_TASK_CMD, consoleTriggerCommand);
                    } catch (Throwable throwable) {
                        logger.error("send error:", throwable);
                        flag = false;
                    }
                }
            }
        }
        return flag;
    }

    public List<ScheduleJobInfo> findAll() {
        return scheduleJobInfoDao.findAll();
    }

    @PreDestroy
    public void shutdown() {
        if (this.scheduleRpcClient != null) {
            this.scheduleRpcClient.shutdown();
        }
    }

}
