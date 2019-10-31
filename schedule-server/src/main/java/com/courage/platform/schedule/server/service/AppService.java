package com.courage.platform.schedule.server.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * 应用管理
 * Created by zhangyong on 2019/10/31.
 */
@Service
public class AppService {

    private final static Logger logger = LoggerFactory.getLogger(AppService.class);

    @PostConstruct
    public void post() {
        logger.info("加载app应用信息");
    }

}
