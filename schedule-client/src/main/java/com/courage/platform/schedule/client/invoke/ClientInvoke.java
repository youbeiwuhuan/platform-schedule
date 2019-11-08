package com.courage.platform.schedule.client.invoke;

/**
 * Created by 王鑫 on 2018/10/17.
 */
public interface ClientInvoke {

    String getServiceId();

    Object invoke(String serviceId, Object[] params) throws Exception;

    boolean isSameClassMethod(ClientInvoke invoker);


}
