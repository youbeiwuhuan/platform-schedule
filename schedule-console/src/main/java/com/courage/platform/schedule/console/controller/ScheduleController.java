package com.courage.platform.schedule.console.controller;

import com.courage.platform.schedule.console.service.AppInfoService;
import com.courage.platform.schedule.console.service.ScheduleJobInfoService;
import com.courage.platform.schedule.dao.domain.Appinfo;
import com.courage.platform.schedule.dao.domain.ScheduleJobInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangyong on 2019/11/5.
 */
@Controller
public class ScheduleController {

    private final static Logger logger = LoggerFactory.getLogger(ScheduleController.class);

    @Autowired
    private ScheduleJobInfoService scheduleJobInfoService;

    @Autowired
    private AppInfoService appInfoService;

    @RequestMapping("/jobinfo")
    public String jobinfo() {
        return "schedule/joblist.index";
    }

    @RequestMapping("/addjobpage")
    public String addjobpage(Model model) {
        List<Appinfo> appinfoList = appInfoService.getAll();
        model.addAttribute("appinfoList", appinfoList);
        return "schedule/jobadd";
    }

    @RequestMapping("/jobinfo/pageList")
    @ResponseBody
    public Map<String, Object> jobinfoPageList(HttpServletRequest httpServletRequest) {
        String start = httpServletRequest.getParameter("start");
        String length = httpServletRequest.getParameter("length"); //类似请求pageSize
        String appName = httpServletRequest.getParameter("appName");
        String jobName = httpServletRequest.getParameter("jobName");
        String jobHandler = httpServletRequest.getParameter("jobHandler");

        Map<String, Object> param = new HashMap<>();
        param.put("start", start);
        param.put("length", length);
        param.put("appName", appName);
        param.put("jobName", jobName);
        param.put("jobHandler", jobHandler);

        List<ScheduleJobInfo> list = scheduleJobInfoService.getPage(param, start, Integer.valueOf(length));

        Integer count = scheduleJobInfoService.count(param);

        Map<String, Object> maps = new HashMap<String, Object>();
        maps.put("recordsTotal", count);        // 总记录数
        maps.put("recordsFiltered", count);        // 总记录数
        maps.put("data", list);                    // 分页列表
        return maps;
    }

    //添加请求
    @RequestMapping("/addJob")
    @ResponseBody
    public Map addJob(@RequestParam Map<String, Object> params) {
        logger.info("addJob params:" + params);
        Appinfo appinfo = appInfoService.getByAppId((String) params.get("appId"));
        params.put("appName", appinfo.getAppName());
        scheduleJobInfoService.insert(params);
        Map map = new HashMap();
        map.put("code", "200");
        return map;
    }

}
