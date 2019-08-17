package com.courage.platform.schedule.console.core.route;


import com.courage.platform.schedule.console.core.route.strategy.ExecutorBroadcast;
import com.courage.platform.schedule.console.core.route.strategy.ExecutorRouteFirst;
import com.courage.platform.schedule.console.core.util.I18nUtil;

/**
 * Created by xuxueli on 17/3/10.
 */
public enum ExecutorRouteStrategyEnum {

    FIRST(I18nUtil.getString("jobconf_route_first"), new ExecutorRouteFirst()),
    SHARDING_BROADCAST(I18nUtil.getString("jobconf_route_shard"), new ExecutorBroadcast());

    ExecutorRouteStrategyEnum(String title, ExecutorRouter router) {
        this.title = title;
        this.router = router;
    }

    private String title;
    private ExecutorRouter router;

    public String getTitle() {
        return title;
    }

    public ExecutorRouter getRouter() {
        return router;
    }

    public static ExecutorRouteStrategyEnum match(String name, ExecutorRouteStrategyEnum defaultItem) {
        if (name != null) {
            for (ExecutorRouteStrategyEnum item : ExecutorRouteStrategyEnum.values()) {
                if (item.name().equals(name)) {
                    return item;
                }
            }
        }
        return defaultItem;
    }

}
