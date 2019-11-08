package com.courage.platform.schedule.console.service;

import com.courage.platform.schedule.client.domain.RSAnnotation;
import com.courage.platform.schedule.client.domain.ScheduleParam;
import com.courage.platform.schedule.client.domain.ScheduleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Created by zhangyong on 2019/11/8.
 */
@Service
public class DemoService {

    private final static Logger logger = LoggerFactory.getLogger(DemoService.class);

    @RSAnnotation(value = "mytest", remark = "我的测试")
    public ScheduleResult demo(ScheduleParam scheduleParam) {
        ScheduleResult scheduleResult = new ScheduleResult(ScheduleResult.SUCCESS_CODE, "正常执行");
        return scheduleResult;
    }

}
