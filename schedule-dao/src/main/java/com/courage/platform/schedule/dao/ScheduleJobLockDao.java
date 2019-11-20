package com.courage.platform.schedule.dao;

import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * Created by zhangyong on 2019/11/20.
 */
@Repository
public interface ScheduleJobLockDao {

    Map selectLockForUpdate();

}
