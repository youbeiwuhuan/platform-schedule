package com.courage.platform.schedule.console;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ServletComponentScan
@EnableScheduling
@ImportResource(locations = "spring-app.xml")
public class ConsoleMainApplication {

    private static Logger logger = LoggerFactory.getLogger(ConsoleMainApplication.class);

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        logger.info("开始启动平台任务调度系统控制台 ^_^");
        SpringApplication.run(ConsoleMainApplication.class, args);
        logger.info("结束启动平台任务调度系统控制台,耗时:" + (System.currentTimeMillis() - start) + "ms");
    }


}
