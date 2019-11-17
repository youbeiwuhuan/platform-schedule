package com.courage.platform.schedule.server.test.timer;

import com.courage.platform.schedule.server.service.recovery.RecoveryMessage;
import com.courage.platform.schedule.server.service.recovery.RecoveryStore;
import org.junit.Test;

import java.util.List;

/**
 * Created by zhangyong on 2019/11/16.
 */
public class FileUnitTest {

    @Test
    public void testAddFile() throws Exception {
        RecoveryStore recoveryStore = new RecoveryStore();
        recoveryStore.start();

        RecoveryMessage recoveryMessage = new RecoveryMessage("1", 2, "hello");
        RecoveryMessage recoveryMessage2 = new RecoveryMessage("2", 2, "hello");
        recoveryStore.put("hello", recoveryMessage);
        recoveryStore.put("hello2", recoveryMessage2);
        Thread.sleep(1000);
        List<RecoveryMessage> list = recoveryStore.queryList(100);

        Thread.sleep(1000000);
    }

}
