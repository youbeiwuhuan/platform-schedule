package com.courage.platform.schedule.core.domain;

/**
 * 调度日志状态
 * Created by zhangyong on 2019/11/4.
 */
public enum TriggerStatusEnum {

    INITIALIZE(-1, "待处理"), SUCCESS(0, "成功"), FAIL(1, "失败");

    private Integer id;
    private String name;

    private TriggerStatusEnum(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
