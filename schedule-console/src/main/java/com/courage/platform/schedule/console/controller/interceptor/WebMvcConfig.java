package com.courage.platform.schedule.console.controller.interceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.annotation.Resource;

/**
 * web mvc config
 *
 * @author xuxueli 2018-04-02 20:48:20
 */
@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {

    @Resource
    private CookieInterceptor cookieInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(cookieInterceptor).addPathPatterns("/**");
        registry.addInterceptor(new PermissionInterceptor()).addPathPatterns("/**");
        super.addInterceptors(registry);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/favicon.ico").addResourceLocations("classpath:/static/favicon.ico");
        registry.addResourceHandler("/**/*.html").addResourceLocations("classpath:/");
        super.addResourceHandlers(registry);
    }

}