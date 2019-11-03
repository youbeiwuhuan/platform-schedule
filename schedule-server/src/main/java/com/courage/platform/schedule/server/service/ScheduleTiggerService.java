package com.courage.platform.schedule.server.service;

import com.courage.platform.schedule.dao.ScheduleJobInfoDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 调度服务
 * Created by zhangyong on 2019/11/3.
 */
@Service
public class ScheduleTiggerService {

    private final static Logger logger = LoggerFactory.getLogger(ScheduleTiggerService.class);

    @Autowired
    private ScheduleJobInfoDao scheduleJobInfoDao;

    public void start() {

    }

    public void shutdown() {

    }

}
