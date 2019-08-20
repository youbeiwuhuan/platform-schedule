package com.courage.platform.schedule.console.core.regcenter;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.courage.platform.client.rpc.regcenter.NacosRegcenterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RegistryController {

    private static final Logger logger = LoggerFactory.getLogger(RegistryController.class);

    public static NacosRegcenterService regcenterService;

    public void setRegcenterService(NacosRegcenterService regcenterService) {
        this.regcenterService = regcenterService;
    }

    public static List<Instance> getInstanceList(String appName, String groupName) throws NacosException {
        return regcenterService.queryAliveInstance(appName);
    }

    public static List<Instance> getInstanceList(String appName) throws NacosException {
        return regcenterService.queryAliveInstance(appName);
    }

}
