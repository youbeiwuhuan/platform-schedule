package com.courage.platform.schedule.demo.config;

import com.courage.platform.schedule.client.manager.SpringPlatformSchedulerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JobConfig {

    private static Logger logger = LoggerFactory.getLogger(JobConfig.class);

    @Value("${platform.consoleAddress}")
    private String consoleAddress;

    @Value("${spring.name}")
    private String appName;

    @Value("${platform.appKey}")
    private String appKey;

    @Bean(initMethod = "start", destroyMethod = "destroy")
    public SpringPlatformSchedulerClient xxlJobExecutor() {
        SpringPlatformSchedulerClient springPlatformSchedulerClient = new SpringPlatformSchedulerClient();
        springPlatformSchedulerClient.setConsoleAddress(consoleAddress);
        springPlatformSchedulerClient.setAppName(appName);
        return springPlatformSchedulerClient;
    }

}