package com.courage.platform.schedule.console.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;

/**
 * Created by 王鑫 on 2019/2/26.
 */
public class TemplateUtil {

    private static Logger logger = LoggerFactory.getLogger(TemplateUtil.class);

    public static String getFailEmailTemplate() {
        String result = "";
        Resource[] resources = new Resource[0];
        try {
            resources = new PathMatchingResourcePatternResolver().getResources("classpath*:template-failmail.html");
            //返回读取指定资源的输入流
            for (int i = 0; i < resources.length; i++) {
                byte[] bytes = new byte[resources[i].getInputStream().available()];
                resources[i].getInputStream().read(bytes);
                result = new String(bytes);
            }
            logger.info("加载模板成功");
        } catch (IOException e) {
            logger.info("读取发送邮件模板异常", e);
        }
        return result;
    }

}
