package com.courage.platform.schedule.server.service;

import com.courage.platform.schedule.dao.AppinfoDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * 应用管理
 * Created by zhangyong on 2019/10/31.
 */
@Service
public class AppService {

    private final static Logger logger = LoggerFactory.getLogger(AppService.class);

    @Autowired
    private AppinfoDao appinfoDao;

    @PostConstruct
    public void post() {
        List list = appinfoDao.findAll();
        logger.info("加载app应用信息");
    }

}
