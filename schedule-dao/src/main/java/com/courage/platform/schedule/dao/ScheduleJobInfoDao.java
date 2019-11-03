package com.courage.platform.schedule.dao;

import com.courage.platform.schedule.dao.domain.Appinfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ScheduleJobInfoDao {

    List<Appinfo> findAll();

}
