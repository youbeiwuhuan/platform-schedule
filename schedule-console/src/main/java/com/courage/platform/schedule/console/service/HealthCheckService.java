package com.courage.platform.schedule.console.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 健康检查服务
 * Created by zhangyong on 2019/11/19.
 */
@Service
public class HealthCheckService {

    private final static Logger logger = LoggerFactory.getLogger(HealthCheckService.class);

    @Transactional
    public void healthCheck() {
        try {
            //连续三次 发送 心跳到服务端 失效 并且 slave 正常 则执行将master 切换成slave

        } catch (Exception e) {
            logger.error("healtch check error:", e);
        }
    }

}
