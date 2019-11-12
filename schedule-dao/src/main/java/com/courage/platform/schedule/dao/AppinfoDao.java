package com.courage.platform.schedule.dao;

import com.courage.platform.schedule.dao.domain.Appinfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;


@Repository
public interface AppinfoDao {

    Appinfo findAppinfoByAppId(@Param("appId") String appId);

    Appinfo findAppinfoById(@Param("id") String id);

    List<Appinfo> findAll();

    int insertAppInfo(Appinfo appInfo);

    int updateAppInfo(Appinfo appInfo);

    int update2(Appinfo appInfo);

    void deleteAppInfoByIds(List<String> appIds);

    List<Appinfo> findPage(Map map);

    Integer count(Map map);

    Integer getMaxAppId();

}
