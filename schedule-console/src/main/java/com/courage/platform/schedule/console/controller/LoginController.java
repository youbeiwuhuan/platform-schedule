package com.courage.platform.schedule.console.controller;

import com.courage.platform.schedule.console.service.LoginService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 登录
 * Created by zhangyong on 2019/11/21.
 */
@Controller
public class LoginController {

    private final static Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private LoginService loginService;

    @Value("${console.username}")
    private String configUsername;

    @Value("${console.password}")
    private String configPassword;

    @RequestMapping("/toLogin")
    public String toLogin() {
        return "login";
    }

    @RequestMapping("/login")
    @ResponseBody
    public Map login(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        String userName = httpServletRequest.getParameter("userName");
        String password = httpServletRequest.getParameter("password");
        if (StringUtils.trimToEmpty(userName).equals(configUsername) && StringUtils.trimToEmpty(password).equals(configPassword)) {
            Map map = new HashMap<>();
            map.put("code", 200);
            map.put("msg", "登录成功");
            return map;
        } else {
            Map map = new HashMap<>();
            map.put("code", 500);
            map.put("msg", "登录失败,账号或者密码错误!");
            return map;
        }
    }

}
