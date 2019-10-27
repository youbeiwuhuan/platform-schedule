package com.courage.platform.schedule.console;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@EnableAutoConfiguration
@ServletComponentScan
@ImportResource(locations = "spring-app.xml")
public class MainApplication extends SpringBootServletInitializer {

    private static Logger logger = LoggerFactory.getLogger(MainApplication.class);

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        logger.info("开始启动hshc-uidemo ^_^");
        SpringApplication.run(MainApplication.class, args);
        logger.info("耗时:" + (System.currentTimeMillis() - start) + "ms");
    }

}
