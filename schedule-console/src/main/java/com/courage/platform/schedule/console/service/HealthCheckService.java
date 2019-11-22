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

    @Transactional
    @Scheduled(initialDelay = 0, fixedRate = 20000)
    public void healthCheck() {
        HashSet<Integer> slaveAvailable = new HashSet<>();
        try {
            scheduleJobLockDao.selectLockForUpdate();

            Integer masterId = null;
            boolean masterAvailable = true;
            List<PlatformNamesrv> platformNamesrvList = platformNamesrvDao.findAll();
            for (PlatformNamesrv platformNamesrv : platformNamesrvList) {
                int trycount = 0;
                boolean flag = false;
                //连续三次 发送 心跳到服务端 失效 并且 slave 正常 则执行将master 切换成slave
                while (trycount < 3 && !flag) {
                    flag = scheduleJobInfoService.heartbeat(platformNamesrv.getNamesrvIp());
                    trycount++;
                    Thread.sleep(150);
                }
                logger.info("namesrvIp:" + platformNamesrv.getNamesrvIp() + " role:" + platformNamesrv.getRole() + " 检测结果:" + flag);
                if (0 == platformNamesrv.getRole()) {
                    masterId = platformNamesrv.getId();
                    if (!flag) {
                        masterAvailable = false;
                    }
                }
                if (1 == platformNamesrv.getRole() && flag) {
                    slaveAvailable.add(platformNamesrv.getId());
                }
            }

            if (!masterAvailable) {
                //选择可靠的slave切换
                if (slaveAvailable.size() > 0) {
                    Map map1 = new HashMap<>();
                    map1.put("id", masterId);
                    map1.put("role", 1);
                    platformNamesrvDao.updateRole(map1);

                    Map map2 = new HashMap<>();
                    map2.put("id", slaveAvailable.toArray()[0]);
                    map2.put("role", 0);
                    platformNamesrvDao.updateRole(map2);
                    logger.info("将slave 切换成master ,map2=" + map2);
                }
            }
        } catch (Exception e) {
            logger.error("healtch check error:", e);
        }
    }

}
