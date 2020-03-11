package com.courage.platform.schedule.common.zookeeper;

/**
 * 存储结构：
 *
 * <pre>
 * /platform
 *     schedule
 *        servers
 *           running (EPHEMERAL)
 *        leader
 *           running (EPHEMERAL)
 * </pre>
 */
public class ZookeeperPathUtils {

    public static final String ZOOKEEPER_SEPARATOR = "/";

    public static final String PLATFORM_ROOT_NODE = ZOOKEEPER_SEPARATOR + "platform";

    public static final String SCHEDULE_NODE = PLATFORM_ROOT_NODE + ZOOKEEPER_SEPARATOR + "schedule";

    public static final String SCHEDULE_SERVER_NODE = SCHEDULE_NODE + ZOOKEEPER_SEPARATOR + "server";

    public static final String LEADER_NODE = SCHEDULE_NODE + ZOOKEEPER_SEPARATOR + "leader";

}
