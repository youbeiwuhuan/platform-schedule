package com.courage.platform.schedule.server.service.recovery;

/**
 * 恢复行动
 * Created by zhangyong on 2019/11/17.
 */
public interface RecoveryAction<T> {

    boolean doAction(T t);

    boolean isReady(T t);

}