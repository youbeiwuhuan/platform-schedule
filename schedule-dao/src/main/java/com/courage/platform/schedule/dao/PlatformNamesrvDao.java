package com.courage.platform.schedule.dao;


import com.courage.platform.schedule.dao.domain.PlatformNamesrv;

import java.util.List;
import java.util.Map;

/**
 * 任务调度名字服务
 * Created by zhangyong on 2019/11/4.
 */
public interface PlatformNamesrvDao {

    List<PlatformNamesrv> findAll();

    PlatformNamesrv getPlatformNamesrvByNamesrvIp(String namesrvIp);

    List<PlatformNamesrv> findPage(Map map);

    Integer count(Map map);

    void insert(Map map);

    void update(Map map);

    PlatformNamesrv getById(Long id);

}
