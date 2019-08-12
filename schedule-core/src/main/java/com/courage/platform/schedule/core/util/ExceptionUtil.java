package com.courage.platform.schedule.core.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @Author 张家荣
 * @Date 2018/9/26
 */
public class ExceptionUtil {
    /**
     * 完整的堆栈信息
     *
     * @param e Exception
     * @return Full StackTrace
     */
    public static String getStackTrace(Exception e) {
        StringWriter sw = null;
        PrintWriter pw = null;
        try {
            sw = new StringWriter();
            pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            pw.flush();
            sw.flush();
        } finally {
            if (sw != null) {
                try {
                    sw.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (pw != null) {
                pw.close();
            }
        }
        return sw.toString();
    }

    /**
     * 按字节截取Exception信息
     *
     * @param str Exception
     * @param bytes length
     * @return Full StackTrace
     */
    public static String splitString(String str, int bytes) {
        int count = 0; //统计字节数
        String reStr = ""; //返回字符串
        if (str == null) {
            return "";
        }
        char[] tempChar = str.toCharArray();
        for (int i = 0; i < tempChar.length; i++) {
            String s1 = str.valueOf(tempChar[i]);
            byte[] b = s1.getBytes();
            count += b.length;
            if (count <= bytes) {
                reStr += tempChar[i];
            }
        }
        return reStr;
    }

}

