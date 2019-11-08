package com.courage.platform.schedule.client.common;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by 王鑫 on 2018/10/19.
 */
public class ScheduleUtils {

    /**
     * 将错误堆栈信息转换成字符串
     *
     * @param e
     * @return
     */
    public static String getlogMsg(Exception e) {
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }

}
