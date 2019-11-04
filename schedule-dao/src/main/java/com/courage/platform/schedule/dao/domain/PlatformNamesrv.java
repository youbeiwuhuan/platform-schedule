package com.courage.platform.schedule.dao.domain;

import java.util.Date;

/**
 * 任务调度名字服务
 * Created by zhangyong on 2019/11/4.
 */
public class PlatformNamesrv {

    private Integer id;

    private String namesrvIp;

    private Integer status;

    private Integer type;

    private Integer role;

    private Date createTime;

    private Date updateTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNamesrvIp() {
        return namesrvIp;
    }

    public void setNamesrvIp(String namesrvIp) {
        this.namesrvIp = namesrvIp;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }


}
