package com.courage.platform.schedule.server.service.recovery;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 恢复数据
 * Created by zhangyong on 2019/11/14.
 */
public class RecoveryMessage<T> {

    private Integer cmd;

    private String key;

    private Long createTime = System.currentTimeMillis();

    private T t;

    //恢复次数，默认恢复十次失败 则删除消息
    private AtomicInteger recoveryCount = new AtomicInteger(0);

    public RecoveryMessage(String key, Integer cmd, T t) {
        this.key = key;
        this.cmd = cmd;
        this.t = t;
    }

    public Integer getCmd() {
        return cmd;
    }

    public void setCmd(Integer cmd) {
        this.cmd = cmd;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public T getT() {
        return t;
    }

    public int incrementRecoveryCount() {
        return recoveryCount.incrementAndGet();
    }

}