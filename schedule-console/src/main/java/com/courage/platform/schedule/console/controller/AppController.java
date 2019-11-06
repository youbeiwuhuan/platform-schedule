package com.courage.platform.schedule.console.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/*
   应用管理
 */
@Controller
public class AppController {

    private final static Logger logger = LoggerFactory.getLogger(AppController.class);

    @RequestMapping("/")
    public String index() {
        return "index";
    }

    @RequestMapping("/applist")
    public String applist() {
        return "appinfo/applist.index";
    }

    @RequestMapping("/applist/pageList")
    @ResponseBody
    public Map<String, Object> appPageList(HttpServletRequest httpServletRequest) {
        String start = httpServletRequest.getParameter("start");
        String length = httpServletRequest.getParameter("length"); //类似请求pageSize
        String appName = httpServletRequest.getParameter("appName");
        List<Map> list = new ArrayList<>();

        Map<String, Object> ele = new HashMap<>();
        ele.put("appId", "10001");
        ele.put("appName", "hshcsmsweb");
        ele.put("remark", "短信web");
        ele.put("createTime", new Date().getTime());
        ele.put("updateTime", new Date().getTime());
        list.add(ele);

        Map<String, Object> ele2 = new HashMap<>();
        ele2.put("appId", "10002");
        ele2.put("appName", "hshcsmsapi");
        ele2.put("remark", "短信api");
        ele2.put("createTime", new Date().getTime());
        ele2.put("updateTime", new Date().getTime());
        list.add(ele2);

        Map<String, Object> ele3 = new HashMap<>();
        ele3.put("appId", "10003");
        ele3.put("appName", "hshcsmsworker");
        ele3.put("remark", "短信worker");
        ele3.put("createTime", new Date().getTime());
        ele3.put("updateTime", new Date().getTime());
        list.add(ele3);

        Map<String, Object> ele4 = new HashMap<>();
        ele4.put("appId", "10004");
        ele4.put("appName", "hshcmdm");
        ele4.put("remark", "主数据系统");
        ele4.put("createTime", new Date().getTime());
        ele4.put("updateTime", new Date().getTime());
        list.add(ele4);

        Map<String, Object> maps = new HashMap<String, Object>();
        maps.put("recordsTotal", 101);        // 总记录数
        maps.put("recordsFiltered", 101);        // 总记录数
        maps.put("data", list);                    // 分页列表
        return maps;
    }

    //添加页面
    @RequestMapping("/applist/addapp")
    public String addAppPage() {
        return "appinfo/appadd";
    }

    //添加请求
    @RequestMapping("/applist/doAdd")
    @ResponseBody
    public Map doAdd(HttpServletRequest httpServletRequest) {
        String appId = httpServletRequest.getParameter("appId");
        String appName = httpServletRequest.getParameter("appName");
        String remark = httpServletRequest.getParameter("remark");
        logger.info("appId:{} appName:{} remark:{}", new Object[]{
                appId,
                appName,
                remark
        });
        Map map = new HashMap();
        map.put("code", "200");
        return map;
    }

}
