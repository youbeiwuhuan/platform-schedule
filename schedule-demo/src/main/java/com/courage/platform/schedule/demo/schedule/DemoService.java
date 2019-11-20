package com.courage.platform.schedule.demo.schedule;

import com.alibaba.fastjson.JSON;
import com.courage.platform.schedule.client.domain.RSAnnotation;
import com.courage.platform.schedule.client.domain.ScheduleParam;
import com.courage.platform.schedule.client.domain.ScheduleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * demo 的服务
 * Created by zhangyong on 2019/11/20.
 */
@Service
public class DemoService {

    private final static Logger logger = LoggerFactory.getLogger(DemoService.class);

    @RSAnnotation(value = "demo.doTestJob")
    public ScheduleResult doTestJob(ScheduleParam scheduleParam) {
        logger.info("doTestJob:" + JSON.toJSONString(scheduleParam));
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
        }
        return new ScheduleResult(ScheduleResult.SUCCESS_CODE, "正常响应");
    }

}
