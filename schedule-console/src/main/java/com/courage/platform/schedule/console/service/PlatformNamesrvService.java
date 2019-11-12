package com.courage.platform.schedule.console.service;

import com.courage.platform.schedule.dao.PlatformNamesrvDao;
import com.courage.platform.schedule.dao.domain.PlatformNamesrv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by zhangyong on 2019/11/12.
 */
@Service
public class PlatformNamesrvService {

    @Autowired
    private PlatformNamesrvDao platformNamesrvDao;

    public PlatformNamesrv getPlatformNamesrvByNamesrvIp(String namesrvIp) {
        return platformNamesrvDao.getPlatformNamesrvByNamesrvIp(namesrvIp);
    }

    public List<PlatformNamesrv> findAll() {
        return platformNamesrvDao.findAll();
    }

}
