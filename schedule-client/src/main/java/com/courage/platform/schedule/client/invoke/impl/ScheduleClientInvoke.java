package com.courage.platform.schedule.client.invoke.impl;

import com.alibaba.fastjson.JSON;
import com.courage.platform.schedule.client.invoke.ClientInvoke;

import java.lang.reflect.Method;

/**
 * 用于存储提供的RPC服务，方便调用
 * Created by 王鑫 on 2018/10/17.
 */
public class ScheduleClientInvoke implements ClientInvoke {
    /**
     * 服务id
     */
    private String serviceId;
    /**
     * 服务备注
     */
    private String remark;
    /**
     * 服务对应的实例对象
     */
    private Object obj;
    /**
     * 被注解标注的方法
     */
    private Method method;

    public ScheduleClientInvoke(String serviceId, String remark, Object obj, Method method) {
        this.serviceId = serviceId;
        this.remark = remark;
        this.obj = obj;
        this.method = method;
    }

    /**
     * 通过服务id调用RPC注解标注方法
     *
     * @param serviceId
     * @param params
     * @return
     * @throws Exception
     */
    @Override
    public Object invoke(String serviceId, Object[] params) throws Exception {
        Object result = null;
        try {
            result = method.invoke(obj, params);
        } catch (Exception e) {
            String errorMsg = "调用RSAnnotation服务异常，服务erviceId:" + serviceId + ",备注：" + remark + "," +
                    "被调用对象：" + obj + ",被调用方法：" + method + ",调用参数" + JSON.toJSONString(params);
            throw new Exception(errorMsg, e);
        }
        return result;
    }


    @Override
    public String getServiceId() {
        return serviceId;
    }

    @Override
    public boolean isSameClassMethod(ClientInvoke invoker) {
        if (invoker instanceof ScheduleClientInvoke) {
            ScheduleClientInvoke other = (ScheduleClientInvoke) invoker;
            //如果对当前类是一致的
            if (other.obj == this.obj || other.obj.getClass().getName().equals(this.obj.getClass().getName())) {
                if (other.method == this.method || other.method.getName().equals(this.method.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "ScheduleClientInvoke{" +
                "serviceId='" + serviceId + '\'' +
                ", remark='" + remark + '\'' +
                ", obj=" + obj +
                ", method=" + method +
                '}';
    }
}
