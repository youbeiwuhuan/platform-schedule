package com.courage.platform.schedule.dao;


import com.courage.platform.schedule.dao.domain.PlatformNamesrv;

import java.util.List;

/**
 * 任务调度名字服务
 * Created by zhangyong on 2019/11/4.
 */
public interface PlatformNamesrvDao {

    List<PlatformNamesrv> findAll();

}
