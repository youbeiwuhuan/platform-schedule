package com.courage.platform.schedule.common.zookeeper;

import java.text.MessageFormat;

/**
 * 存储结构：
 *
 * <pre>
 * /platform
 *     schedule
 *        servers
 *            172.20.10.9:12999 (EPHEMERAL)
 *        leader
 *            0000000024 (EPHEMERAL)
 * </pre>
 */
public class ZookeeperPathUtils {

    public static final String ZOOKEEPER_SEPARATOR = "/";

    public static final String PLATFORM_ROOT_NODE = ZOOKEEPER_SEPARATOR + "platform";

    public static final String SCHEDULE_NODE = PLATFORM_ROOT_NODE + ZOOKEEPER_SEPARATOR + "schedule";

    public static final String SCHEDULE_SERVER_NODE = SCHEDULE_NODE + ZOOKEEPER_SEPARATOR + "server";

    public static final String SCHEDULE_LEADER_NODE = SCHEDULE_NODE + ZOOKEEPER_SEPARATOR + "leader";

    public static final String LEADER_CHILD_FORMAT = SCHEDULE_LEADER_NODE + ZOOKEEPER_SEPARATOR + "{0}";

    public static String getLeaderChildPath(String childId) {
        return MessageFormat.format(LEADER_CHILD_FORMAT, childId);
    }

}
