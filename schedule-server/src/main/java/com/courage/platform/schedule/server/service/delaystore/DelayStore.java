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

    //每一个映射文件的大小
    private final Integer DEFAULT_MAPPED_FILE_SIZE = 50 * 1024 * 1024;

    private final String PLATFORM = "platform";

    private final String SCHEDULE = "schedule";

    private final String baseDir = System.getProperty("user.home") + File.separator + PLATFORM + File.separator + SCHEDULE;

    private volatile boolean inited = false;

    private MmapFileList mmapFileList;

    public DelayStore() {
        DefaultMmapFile.ensureDirOK(baseDir);
        logger.info("延迟存储目录:" + baseDir);
    }

    public void start() {
        this.mmapFileList = new MmapFileList(baseDir, DEFAULT_MAPPED_FILE_SIZE);
        this.mmapFileList.load();
        this.inited = true;
    }

    public Long append(byte[] data) {
        return this.mmapFileList.append(data);
    }

    public void shutdown() {
        if (this.mmapFileList != null) {
            this.mmapFileList.shutdown(10000);
        }
    }


}
