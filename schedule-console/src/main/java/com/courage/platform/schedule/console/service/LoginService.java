package com.courage.platform.schedule.console.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 登录服务
 * Created by zhangyong on 2019/11/21.
 */
@Service
public class LoginService {

    private final static Logger logger = LoggerFactory.getLogger(LoginService.class);

    private final static String LOGIN_IDENTITY_KEY = "scheduleToken";

    public String createToken(String username) {
        //aes128(username + : password)
        return null;
    }

}
