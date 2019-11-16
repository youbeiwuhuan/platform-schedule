package com.courage.platform.schedule.server.test.timer;

import com.courage.platform.schedule.server.service.delaystore.file.MmapFileList;
import org.junit.Test;

/**
 * Created by zhangyong on 2019/11/16.
 */
public class FileUnitTest {

    private static String storePath = System.getProperty("user.home") + "/schedule";

    @Test
    public void testAddFile() {
        MmapFileList mmapFileList = new MmapFileList(storePath, 1024 * 1024);
        mmapFileList.append("mylife".getBytes());

        mmapFileList.commit(0);
    }

}
