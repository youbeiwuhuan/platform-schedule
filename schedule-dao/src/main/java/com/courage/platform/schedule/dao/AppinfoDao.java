package com.courage.platform.schedule.dao;

import com.courage.platform.schedule.dao.domain.Appinfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface AppinfoDao {

    Appinfo findAppinfoByAppId(@Param("appId") String appId);

    List<Appinfo> findAll();

    int insertAppInfo(Appinfo appInfo);

    int updateAppInfo(Appinfo appInfo);

    void deleteAppInfoByIds(List<String> appIds);

}
