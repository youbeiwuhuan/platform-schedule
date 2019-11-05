package com.courage.platform.schedule.console.service;

import com.courage.platform.schedule.dao.ScheduleJobInfoDao;
import com.courage.platform.schedule.dao.domain.ScheduleJobInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by zhangyong on 2019/11/5.
 */
@Service
public class ScheduleJobInfoService {

    private final static Logger logger = LoggerFactory.getLogger(ScheduleJobInfoService.class);

    @Autowired
    private ScheduleJobInfoDao scheduleJobInfoDao;

    public List<ScheduleJobInfo> getPage(Map<String, Object> param, String start, Integer pageSize) {
        param.put("start", Integer.valueOf(start));
        param.put("pageSize", pageSize);
        return scheduleJobInfoDao.findPage(param);
    }

    public Integer count(Map<String, Object> param) {
        return scheduleJobInfoDao.count(param);
    }

}
