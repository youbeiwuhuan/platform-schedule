package com.courage.platform.schedule.console.core.trigger;

import com.courage.platform.schedule.console.core.model.XxlJobGroup;
import com.courage.platform.schedule.console.core.model.XxlJobInfo;
import com.courage.platform.schedule.console.core.route.ExecutorRouteStrategyEnum;
import com.courage.platform.schedule.console.core.schedule.XxlJobDynamicScheduler;
import com.courage.platform.schedule.core.biz.ExecutorBiz;
import com.courage.platform.schedule.core.biz.model.ReturnT;
import com.courage.platform.schedule.core.biz.model.TriggerParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * xxl-job trigger
 * Created by xuxueli on 17/7/13.
 */
public class XxlJobTrigger {
    private static Logger logger = LoggerFactory.getLogger(XxlJobTrigger.class);

    /**
     * trigger job
     *
     * @param jobId
     */
    public static void trigger(int jobId) {
        XxlJobInfo jobInfo = null;
        XxlJobGroup group = null;
        ExecutorRouteStrategyEnum executorRouteStrategyEnum = null;
        try {
            // load data，job info
            jobInfo = XxlJobDynamicScheduler.xxlJobInfoDao.loadById(jobId);
            // group info
            group = XxlJobDynamicScheduler.xxlJobGroupDao.load(jobInfo.getJobGroup());
            // route strategy
            executorRouteStrategyEnum = ExecutorRouteStrategyEnum.match(jobInfo.getExecutorRouteStrategy(), null);
            executorRouteStrategyEnum.getRouter().routeRun(jobInfo, group);
        } catch (Throwable e) {
            logger.error(">>>>>>>>>>>> trigger fail，jobId：" + jobId + ",jobInfo：" + JSON.toJSONString(jobInfo) + ",jobGroup:" + JSON.toJSONString(jobInfo.getJobGroup()), e);
            return;
        }
    }

    /**
     * 旧版HTTPRPC服务
     *
     * @param triggerParam
     * @param address
     * @return ReturnT.content: final address
     */
    public static ReturnT<String> runExecutor(TriggerParam triggerParam, String address) {
        logger.info("triggerParam:" + JSONUtil.toString(triggerParam));
        ReturnT<String> runResult = null;
        try {
            ExecutorBiz executorBiz = XxlJobDynamicScheduler.getExecutorBiz(address);
            runResult = executorBiz.run(triggerParam);
        } catch (Exception e) {
            logger.error(">>>>>>>>>>> xxl-job trigger error, please check if the executor[{}] is running.", address, e);
            runResult = new ReturnT<String>(ReturnT.FAIL_CODE, "" + e);
        }
        return runResult;
    }

}
