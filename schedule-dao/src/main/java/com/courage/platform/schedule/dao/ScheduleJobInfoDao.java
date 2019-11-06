package com.courage.platform.schedule.dao;

import com.courage.platform.schedule.dao.domain.ScheduleJobInfo;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;


@Repository
public interface ScheduleJobInfoDao {

    List<ScheduleJobInfo> findAll();

    List<ScheduleJobInfo> findPage(Map map);

    Integer count(Map map);

    Integer insert(Map map);

    ScheduleJobInfo getById(String id);

    void update(Map map);

    void delete(Map map);

}
