package com.courage.platform.schedule.server.test.timer;

import com.courage.platform.schedule.server.service.recovery.RecoveryStore;
import org.junit.Test;

/**
 * Created by zhangyong on 2019/11/16.
 */
public class FileUnitTest {

    @Test
    public void testAddFile() throws InterruptedException {
        RecoveryStore recoveryStore = new RecoveryStore();
        recoveryStore.start();

        recoveryStore.append("hello".getBytes());
        Thread.sleep(1000000);
    }

}
