package com.courage.platform.schedule.console.service;

import com.courage.platform.schedule.dao.PlatformNamesrvDao;
import com.courage.platform.schedule.dao.ScheduleJobLockDao;
import com.courage.platform.schedule.dao.domain.PlatformNamesrv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Transactional
    @Scheduled(initialDelay = 0, fixedRate = 20000)
    public void healthCheck() {
        try {
            //连续三次 发送 心跳到服务端 失效 并且 slave 正常 则执行将master 切换成slave
            scheduleJobLockDao.selectLockForUpdate();

            List<PlatformNamesrv> platformNamesrvList = platformNamesrvDao.findAll();
            
            for (PlatformNamesrv platformNamesrv : platformNamesrvList) {

            }

        } catch (Exception e) {
            logger.error("healtch check error:", e);
        }
    }

}
