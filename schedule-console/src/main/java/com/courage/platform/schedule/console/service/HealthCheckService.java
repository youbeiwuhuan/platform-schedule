package com.courage.platform.schedule.console.service;

import com.courage.platform.schedule.dao.PlatformNamesrvDao;
import com.courage.platform.schedule.dao.ScheduleJobLockDao;
import com.courage.platform.schedule.dao.domain.PlatformNamesrv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * 健康检查服务(master slave 模式)
 * Created by zhangyong on 2019/11/19.
 */
@Service
public class HealthCheckService {

    private final static Logger logger = LoggerFactory.getLogger(HealthCheckService.class);

    @Autowired
    private ScheduleJobLockDao scheduleJobLockDao;

    @Autowired
    private PlatformNamesrvDao platformNamesrvDao;

    @Autowired
    private ScheduleJobInfoService scheduleJobInfoService;

    @Value("${console.healthCheck:false}")
    private boolean healthCheck;

    @Transactional
    @Scheduled(initialDelay = 0, fixedRate = 20000)
    public void healthCheck() {
        if (!healthCheck) {
            return;
        }
    }

}
