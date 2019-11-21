package com.courage.platform.schedule.console.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 登录
 * Created by zhangyong on 2019/11/21.
 */
@Controller
public class LoginController {

    private final static Logger logger = LoggerFactory.getLogger(LoginController.class);

    @RequestMapping("/toLogin")
    public String toLogin() {
        return "login";
    }

    @RequestMapping("/login")
    @ResponseBody
    public Map login(HttpServletRequest httpServletRequest) {
        String userName = httpServletRequest.getParameter("userName");
        String password = httpServletRequest.getParameter("password");

        Map map = new HashMap<>();
        map.put("code", 200);
        map.put("msg", "登录成功");

        return map;
    }

}
