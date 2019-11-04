package com.courage.platform.schedule.server.service;

import com.courage.platform.schedule.dao.domain.ScheduleJobInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 调度服务
 * Created by zhangyong on 2019/11/3.
 */
@Service
public class ScheduleTriggerService {

    private final static Logger logger = LoggerFactory.getLogger(ScheduleTriggerService.class);

    @Autowired
    private ScheduleJobInfoService scheduleJobInfoService;

    @Value("${task_trigger_mode:0}")
    private Integer taskTriggerMode;

    @PostConstruct
    public void start() {
        //检测当前ip是否支持启动
        ConcurrentHashMap<Long, ScheduleJobInfo> jobInfoCache = scheduleJobInfoService.getJobInfoCache();
    }

    @PreDestroy
    public void shutdown() {
    }

}
