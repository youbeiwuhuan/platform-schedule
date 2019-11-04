package com.courage.platform.schedule.server.service;

import com.courage.platform.schedule.server.service.mode.DatabaseTriggerMode;
import com.courage.platform.schedule.server.service.mode.RaftTriggerMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * 调度服务
 * Created by zhangyong on 2019/11/3.
 */
@Service
public class ScheduleTriggerService {

    private final static Logger logger = LoggerFactory.getLogger(ScheduleTriggerService.class);

    @Autowired
    private DatabaseTriggerMode databaseTriggerMode;

    @Autowired
    private RaftTriggerMode raftTriggerMode;

    @Value("${task_trigger_mode:0}")
    private int taskTriggerMode;

    @PostConstruct
    public void start() {
        //数据库模式 主从模式 任务跑在主机上
        if (taskTriggerMode == 0) {
            databaseTriggerMode.start();
        }
        //raft 协议，竞选leader,通过leader分配不同的任务到不同的机器上执行
        if (taskTriggerMode == 1) {
            raftTriggerMode.start();
        }
    }

    @PreDestroy
    public void shutdown() {
        //数据库模式 主从模式 任务跑在主机上
        if (taskTriggerMode == 0) {
            databaseTriggerMode.shutdown();
        }
        //raft 协议，竞选leader,通过leader分配不同的任务到不同的机器上执行
        if (taskTriggerMode == 1) {
            raftTriggerMode.shutdown();
        }
    }

}
