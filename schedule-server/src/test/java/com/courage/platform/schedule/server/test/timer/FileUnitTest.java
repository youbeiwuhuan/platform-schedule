package com.courage.platform.schedule.server.test.timer;

import com.courage.platform.schedule.server.service.delaystore.DelayStore;
import org.junit.Test;

/**
 * Created by zhangyong on 2019/11/16.
 */
public class FileUnitTest {

    @Test
    public void testAddFile() throws InterruptedException {
        DelayStore delayStore = new DelayStore();
        delayStore.start();

        delayStore.append("hello".getBytes());
        Thread.sleep(1000000);
    }

}
