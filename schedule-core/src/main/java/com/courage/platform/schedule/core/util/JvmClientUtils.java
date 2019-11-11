package com.courage.platform.schedule.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JvmClientUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(JvmClientUtils.class);

    public static final String INNER_IP_PATTERN = "((192\\.168|172\\.([1][6-9]|[2]\\d|3[01]))" + "(\\.([2][0-4]\\d|[2][5][0-5]|[01]?\\d?\\d)){2}|" + "^(\\D)*10(\\.([2][0-4]\\d|[2][5][0-5]|[01]?\\d?\\d)){3})";

    public static final String LOCAL_IP = initIP();

    public static final String LOCAL_HOST = initLocalHostName();

    /**
     * 获取本机ip地址
     */
    public static String getLocalIp() {
        return LOCAL_IP;
    }

    /**
     * 获取本地机器名
     */
    public static String getLocalHostName() {
        return LOCAL_HOST;
    }

    public static String initLocalHostName() {
        String hostName = System.getenv("COMPUTERNAME");
        if (hostName == null) {
            try {
                hostName = (InetAddress.getLocalHost()).getHostName();
            } catch (UnknownHostException e) {
                LOGGER.error("get hostname error", e);
                String host = e.getMessage();
                if (host != null) {
                    int colon = host.indexOf(':');
                    if (colon > 0) {
                        hostName = host.substring(0, colon);
                    }
                }
            }
        }
        LOGGER.info("get local hostName ：" + hostName);
        return hostName;
    }

    /**
     * 判断是否是内网IP
     *
     * @param ip
     * @return
     */
    public static boolean isInnerIP(String ip) {
        if (ip == null) {
            return false;
        } else if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
            return true;
        }
        Pattern reg = Pattern.compile(INNER_IP_PATTERN);
        Matcher match = reg.matcher(ip);

        return match.find();
    }

    public static String initIP() {
        try {
            //根据网卡取本机配置的IP
            Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
            String ip = null;
            a:
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = netInterfaces.nextElement();
                Enumeration<InetAddress> ips = ni.getInetAddresses();
                while (ips.hasMoreElements()) {
                    InetAddress ipObj = ips.nextElement();
                    if (ipObj.isSiteLocalAddress()) {
                        ip = ipObj.getHostAddress();
                        break a;
                    }
                }
            }
            return ip;
        } catch (Exception e) {
            LOGGER.error("initIP IS ERROR!", e);
            return null;
        }
    }

    public static final String LOCALHOST = localhost();

    private static String localhost() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Throwable e) {
            try {
                String candidatesHost = getLocalhostByNetworkInterface();
                if (candidatesHost != null) {
                    return candidatesHost;
                }
            } catch (Exception ignored) {
            }
            throw new RuntimeException("InetAddress java.net.InetAddress.getLocalHost() throws UnknownHostException", e);
        }
    }

    public static String getLocalhostByNetworkInterface() throws SocketException {
        List<String> candidatesHost = new ArrayList<String>();
        Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();

        while (enumeration.hasMoreElements()) {
            NetworkInterface networkInterface = enumeration.nextElement();
            // Workaround for docker0 bridge
            if ("docker0".equals(networkInterface.getName()) || !networkInterface.isUp()) {
                continue;
            }
            Enumeration<InetAddress> addrs = networkInterface.getInetAddresses();
            while (addrs.hasMoreElements()) {
                InetAddress address = addrs.nextElement();
                if (address.isLoopbackAddress()) {
                    continue;
                }
                //ip4 highter priority
                if (address instanceof Inet6Address) {
                    candidatesHost.add(address.getHostAddress());
                    continue;
                }
                return address.getHostAddress();
            }
        }

        if (!candidatesHost.isEmpty()) {
            return candidatesHost.get(0);
        }
        return null;
    }

    public static long getPID() {
        String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
        if (processName != null && processName.length() > 0) {
            try {
                return Long.parseLong(processName.split("@")[0]);
            } catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }

    public static String getJmvClientId() {
        return LOCAL_IP + "@" + getPID() + "@" + UUID.randomUUID().toString();
    }

}
