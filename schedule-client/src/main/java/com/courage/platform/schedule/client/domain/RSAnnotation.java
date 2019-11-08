package com.courage.platform.schedule.client.domain;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 远程方法同步调用时间
 * 张勇
 * 2019-07-05
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RSAnnotation {

    //远程服务id
    String value();

    //备注
    String remark() default "";

}