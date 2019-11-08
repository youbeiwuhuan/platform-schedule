package com.courage.platform.schedule.client.manager;

import com.courage.platform.schedule.client.domain.RSAnnotation;
import com.courage.platform.schedule.client.invoke.ClientInvoke;
import com.courage.platform.schedule.client.invoke.impl.ScheduleClientInvoke;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by 王鑫 on 2018/10/17.
 */
public class PlatformScheduleResolver {

    private static final Logger logger = LoggerFactory.getLogger(PlatformScheduleResolver.class);
    private static final Map<String, ClientInvoke> invokerMap = new ConcurrentHashMap<String, ClientInvoke>(512);


    public static void getAbstractBean(Map<String, Object> serviceBeanMap) {
        //遍历每个Component注解标注的bean
        for (Map.Entry<String, Object> entry : serviceBeanMap.entrySet()) {
            Object object = entry.getValue();
            Class<? extends Object> clazz = object.getClass();
            getRSAnnotationMethod(clazz.getMethods(), object);
        }
    }

    /**
     * 获取标注了@RSAnnotation注解的方法，并添加到全局变量Map中
     *
     * @param methods 当前类的所有方法
     * @param object  当了类的实例
     */
    private static void getRSAnnotationMethod(Method[] methods, Object object) {
        for (Method method : methods) {
            //使用AnnotationUtils.findAnnotation拿到代理类中的注解
            RSAnnotation rsAnnotation = AnnotationUtils.findAnnotation(method, RSAnnotation.class);
            //如果当前方法被@RSAnnotation注解标注了，则添加当前serviceId和当前对象到全局变量Map中
            if (rsAnnotation != null) {
                ScheduleClientInvoke scheduleClientInvoke = new ScheduleClientInvoke(rsAnnotation.value(), rsAnnotation.remark(), object, method);
                addInvoker(scheduleClientInvoke);
            }
        }
    }

    public static ClientInvoke getInvoker(String serviceId) {
        //将服务id转换为小写
        serviceId = serviceId.toLowerCase();
        return invokerMap.get(serviceId);
    }

    /**
     * 将保存RPC服务的ClientInvoke对象保存到缓存Map中
     *
     * @param clientInvoke
     */
    private static synchronized void addInvoker(ClientInvoke clientInvoke) {
        //将服务id转换为小写
        String serviceId = clientInvoke.getServiceId().toLowerCase();
        ClientInvoke temp = invokerMap.get(serviceId);
        //如果全局变量Map已经存在serviceId
        if (temp != null) {
            //如果已经存在当前方法,则覆盖
            if (temp.isSameClassMethod(clientInvoke)) {
                logger.error("clientInvoke is reload! serviceId=" + serviceId);
                invokerMap.put(serviceId, clientInvoke);
            } else {
                logger.error("ClientInvoke定义重复" + temp);
            }
        }
        invokerMap.put(serviceId, clientInvoke);
    }

}
