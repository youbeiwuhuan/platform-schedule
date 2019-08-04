package com.courage.platform.schedule.console.core.route.strategy;

import com.courage.platform.schedule.console.core.route.ExecutorRouter;
import com.courage.platform.schedule.core.biz.model.ReturnT;
import com.courage.platform.schedule.core.biz.model.TriggerParam;

import java.util.List;

/**
 * Created by xuxueli on 17/3/10.
 */
public class ExecutorRouteFirst extends ExecutorRouter {

    @Override
    public ReturnT<String> route(TriggerParam triggerParam, List<String> addressList){
        return new ReturnT<String>(addressList.get(0));
    }

}
