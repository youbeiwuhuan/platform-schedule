package com.courage.platform.schedule.server.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * 调度服务
 * Created by zhangyong on 2019/11/3.
 */
@Service
public class ScheduleTriggerService {

    private final static Logger logger = LoggerFactory.getLogger(ScheduleTriggerService.class);

    @Autowired
    private ScheduleJobInfoService scheduleJobInfoService;

    @PostConstruct
    public void start() {
    }

    @PreDestroy
    public void shutdown() {
    }

}
