package com.courage.platform.schedule.console.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by zhangyong on 2019/11/5.
 */
@Controller
public class ScheduleController {

    private final static Logger logger = LoggerFactory.getLogger(ScheduleController.class);

    @RequestMapping("/jobinfo")
    public String jobinfo() {
        return "schedule/joblist.index";
    }

    @RequestMapping("/addjobpage")
    public String addjobpage() {
        return "schedule/jobadd";
    }

    @RequestMapping("/jobinfo/pageList")
    @ResponseBody
    public Map<String, Object> jobinfoPageList(HttpServletRequest httpServletRequest) {
        String start = httpServletRequest.getParameter("start");
        String length = httpServletRequest.getParameter("length"); //类似请求pageSize
        String appName = httpServletRequest.getParameter("appName");
        logger.info("start:{} length:{} appName:{}", new Object[]{start, length, appName});
        List<Map> list = new ArrayList<>();

        Map<String, Object> ele = new HashMap<>();
        ele.put("id", "10001");
        ele.put("appName", "hshcsmsweb");
        ele.put("jobName", "测试1");
        ele.put("jobCron", "*/16 * * * * ?");
        ele.put("status", "0");
        list.add(ele);

        Map<String, Object> ele2 = new HashMap<>();
        ele2.put("id", "10002");
        ele2.put("appName", "hshcsmsapi");
        ele2.put("jobName", "测试2");
        ele2.put("jobCron", "*/22 * * * * ?");
        ele2.put("status", "0");
        list.add(ele2);

        Map<String, Object> ele3 = new HashMap<>();
        ele3.put("id", "10003");
        ele3.put("appName", "hshcsmsworker");
        ele3.put("jobName", "ceshi3");
        ele3.put("remark", "测试3");
        ele3.put("jobCron", "*/32 * * * * ?");
        ele3.put("status", "0");
        list.add(ele3);

        Map<String, Object> maps = new HashMap<String, Object>();
        maps.put("recordsTotal", 101);        // 总记录数
        maps.put("recordsFiltered", 101);        // 总记录数
        maps.put("data", list);                    // 分页列表
        return maps;
    }

}
