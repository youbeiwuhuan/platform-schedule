package com.courage.platform.schedule.console.service;

import com.courage.platform.schedule.dao.AppinfoDao;
import com.courage.platform.schedule.dao.domain.Appinfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by zhangyong on 2019/11/5.
 */
@Service
public class AppInfoService {

    private static final Logger logger = LoggerFactory.getLogger(AppInfoService.class);

    @Autowired
    private AppinfoDao appinfoDao;

    public List<Appinfo> getAll() {
        return appinfoDao.findAll();
    }

    public Appinfo getByAppId(String id) {
        return appinfoDao.findAppinfoByAppId(id);
    }

}
