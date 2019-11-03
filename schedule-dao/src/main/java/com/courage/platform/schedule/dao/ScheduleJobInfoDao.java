package com.courage.platform.schedule.dao;

import com.courage.platform.schedule.dao.domain.ScheduleJobInfo;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ScheduleJobInfoDao {

    List<ScheduleJobInfo> findAll();

}
