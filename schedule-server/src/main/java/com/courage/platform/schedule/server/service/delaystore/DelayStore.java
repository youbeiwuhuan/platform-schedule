package com.courage.platform.schedule.server.service.delaystore;

import com.courage.platform.schedule.server.service.delaystore.file.DefaultMmapFile;
import com.courage.platform.schedule.server.service.delaystore.file.MmapFileList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * 延迟存储容器
 * Created by zhangyong on 2019/11/14.
 */
public class DelayStore {

    private final static Logger logger = LoggerFactory.getLogger(DelayStore.class);

    private final String PLATFORM = "platform";

    private final String SCHEDULE = "schedule";

    private final String baseDir = System.getProperty("user.home") + File.separator + PLATFORM + File.separator + SCHEDULE;

    private MmapFileList mmapFileList;

    public DelayStore() {
        DefaultMmapFile.ensureDirOK(baseDir);
        this.mmapFileList = new MmapFileList(baseDir, 100 * 1024 * 1024);
        this.mmapFileList.load();
    }

    public static void main(String[] args) {
        DelayStore delayStore = new DelayStore();
    }

}
