package com.courage.platform.schedule.client.manager;

import com.courage.platform.schedule.client.rpc.controller.ScheduleClientController;
import com.courage.platform.schedule.client.service.CallbackThreadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by 王鑫 on 2018/9/27.
 */
public class PlatformSchedulerClient implements ApplicationContextAware, ApplicationListener, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(PlatformSchedulerClient.class);

    private ApplicationContext applicationContext = null;

    private String appName;

    private String appKey;

    private final static ScheduleClientController scheduleClientController = ScheduleClientController.getSingleInstance();

    private final static CallbackThreadService threadService = CallbackThreadService.getSingleInstance();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (!(event instanceof ContextRefreshedEvent)) {
            return;
        }
        try {
            //获取Service注解标注的所有bean
            Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(Component.class);
            //扫描所有的远程服务的注解(放入本地缓存中)
            PlatformScheduleResolver.getAbstractBean(serviceBeanMap);
            //实例化，启动Scheduler client
            logger.info("启动调度服务成功!");
        } catch (Exception e) {
            logger.error("启动调度服务失败!", e);
        }
    }

    @Override
    public void destroy() {
        //关闭存储执行结果的线程池
        threadService.close();
        //关闭发送执行结果的tcp客户端
        scheduleClientController.close();
    }

}

