package com.courage.platform.schedule.console.service;

import com.courage.platform.schedule.dao.AppinfoDao;
import com.courage.platform.schedule.dao.domain.Appinfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangyong on 2019/11/5.
 */
@Service
public class AppInfoService {

    private static final Logger logger = LoggerFactory.getLogger(AppInfoService.class);

    private static final int APP_ID_STEP = 1000;

    @Autowired
    private AppinfoDao appinfoDao;

    public List<Appinfo> getAll() {
        return appinfoDao.findAll();
    }

    public Appinfo getByAppId(String id) {
        return appinfoDao.findAppinfoByAppId(id);
    }

    public List<Appinfo> getPage(Map param, String start, Integer pageSize) {
        param.put("start", Integer.valueOf(start));
        param.put("pageSize", pageSize);
        return appinfoDao.findPage(param);
    }

    public Integer count(Map param) {
        return appinfoDao.count(param);
    }

    public Integer getMaxAppId() {
        Integer maxAppId = appinfoDao.getMaxAppId();
        if (maxAppId == null) {
            return APP_ID_STEP + 1;
        }
        return maxAppId + 1;
    }

    public void addAppInfo(Appinfo appinfo) {
        appinfo.setAppId(String.valueOf(getMaxAppId()));
        appinfo.setCreateTime(new Date());
        appinfo.setUpdateTime(new Date());
        appinfoDao.insertAppInfo(appinfo);
    }

}
