package com.courage.platform.schedule.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Created by zhangyong on 2019/11/20.
 */
@SpringBootApplication(scanBasePackages = "com.courage.platform.schedule")
@ServletComponentScan
@EnableScheduling
public class DemoMainApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoMainApplication.class, args);
    }

}
