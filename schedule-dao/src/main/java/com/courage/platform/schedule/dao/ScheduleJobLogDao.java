package com.courage.platform.schedule.dao;

import com.courage.platform.schedule.dao.domain.ScheduleJobLog;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;


@Repository
public interface ScheduleJobLogDao {

    Integer insert(ScheduleJobLog scheduleJobLog);

    void update(Map map);

    List<ScheduleJobLog> findPage(Map map);

    Integer count(Map map);

    ScheduleJobLog findById(Long id);


}
