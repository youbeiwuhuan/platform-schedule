package com.courage.platform.schedule.dao;

import com.courage.platform.schedule.dao.domain.ScheduleJobInfo;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;


@Repository
public interface ScheduleJobInfoDao {

    List<ScheduleJobInfo> findAll();

    List<ScheduleJobInfo> findPage(Map map);

}
