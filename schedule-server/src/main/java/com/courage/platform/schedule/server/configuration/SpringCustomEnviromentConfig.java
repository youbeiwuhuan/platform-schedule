package com.courage.platform.schedule.server.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import java.io.IOException;
import java.util.Properties;

/**
 * 模仿springboot的各种环境配置信息
 * Created by zhangyong on 2019/11/1.
 */
public class SpringCustomEnviromentConfig extends PropertyPlaceholderConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(SpringCustomEnviromentConfig.class);

    @Override
    protected void loadProperties(Properties props) throws IOException {
        logger.info("pros:" + props);
    }

}
