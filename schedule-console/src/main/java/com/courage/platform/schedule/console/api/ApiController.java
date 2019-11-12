package com.courage.platform.schedule.console.api;

import com.courage.platform.schedule.console.service.PlatformNamesrvService;
import com.courage.platform.schedule.dao.domain.PlatformNamesrv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
   应用管理
 */
@RestController
public class ApiController {

    private final static Logger logger = LoggerFactory.getLogger(ApiController.class);

    @Autowired
    private PlatformNamesrvService platformNamesrvService;

    @RequestMapping("/address")
    public Object scheduleserver() {
        Map map = new HashMap<>();
        map.put("code", 0);

        List<PlatformNamesrv> data = platformNamesrvService.findAll();

        map.put("data", data);

        return map;
    }

}
