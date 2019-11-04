package com.courage.platform.schedule.dao.domain;

/**
 * 调度日志状态
 * Created by zhangyong on 2019/11/4.
 */
public enum ScheduleLogStatusEnum {

    INITIALIZE(-1, "待处理"), SUCCESS(0, "成功"), FAIL(1, "失败"), PART_FAIL(2, "部分失败");

    private Integer id;
    private String name;

    private ScheduleLogStatusEnum(Integer id, String name) {
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
