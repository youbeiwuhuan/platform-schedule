package com.courage.platform.schedule.server.service;

import com.courage.platform.schedule.dao.AppinfoDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * 应用管理
 * Created by zhangyong on 2019/10/31.
 */
@Service
public class AppInfoService {

    private final static Logger logger = LoggerFactory.getLogger(AppInfoService.class);

    @Autowired
    private AppinfoDao appinfoDao;

    public void post() {
        List list = appinfoDao.findAll();
    }

}
