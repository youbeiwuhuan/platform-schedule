package com.courage.platform.schedule.dao;

import com.courage.platform.schedule.dao.domain.ScheduleJobLog;
import org.springframework.stereotype.Repository;


@Repository
public interface ScheduleJobLogDao {

    Integer insert(ScheduleJobLog scheduleJobLog);

}
