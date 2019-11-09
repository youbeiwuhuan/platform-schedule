package com.courage.platform.schedule.console.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
   应用管理
 */
@RestController
public class ApiController {

    private final static Logger logger = LoggerFactory.getLogger(ApiController.class);

    @RequestMapping("/address")
    public Object scheduleserver() {
        Map map = new HashMap<>();
        map.put("code", 0);

        List<Map> data = new ArrayList<>();
        Map ele = new HashMap();
        ele.put("type", "0");
        ele.put("namesrvIp", "127.0.0.12999");
        ele.put("status", "0");

        data.add(ele);

        map.put("data", data);

        return map;
    }

}
