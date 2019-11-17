package com.courage.platform.schedule.server.service.recovery;

/**
 * 恢复行动
 * Created by zhangyong on 2019/11/17.
 */
public interface RecoveryAction {

    boolean doAction(RecoveryMessage recoveryMessage);
    
}