package com.courage.platform.schedule.console.core.rpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by 王鑫 on 2018/10/24.
 */
public class RegistryController {

    private static final Logger logger = LoggerFactory.getLogger(NetComClientProxy.class);

    private static PlatformRegistryClient registryClient = new PlatformRegistryClient();

    private static RegistryController instance = new RegistryController();

    private RegistryController() {

    }

    public static RegistryController getSingleInstance() {
        return instance;
    }

    /**
     * 获取appName应用提供的注册信息
     *
     * @param appName
     * @return
     */
    public List<RegistryInstance> getInstanceListByAppName(String appName, ServiceGroupEnum groupEnum) {
        List<RegistryInstance> instanceList = null;
        try {
            instanceList = registryClient.getInstanceList(appName, groupEnum);
        } catch (Exception e) {
            logger.error("从注册中心获取服务异常，请求参数：appName=" + appName + ",serviceGroupEnum" + groupEnum, e);
        }
        return instanceList;
    }

}
