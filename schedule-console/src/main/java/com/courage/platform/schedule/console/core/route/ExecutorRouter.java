package com.courage.platform.schedule.console.core.route;

import com.courage.platform.schedule.console.core.enums.ExecutorFailStrategyEnum;
import com.courage.platform.schedule.console.core.model.XxlJobGroup;
import com.courage.platform.schedule.console.core.model.XxlJobInfo;
import com.courage.platform.schedule.console.core.model.XxlJobLog;
import com.courage.platform.schedule.console.core.schedule.XxlJobDynamicScheduler;
import com.courage.platform.schedule.console.core.thread.JobFailMonitorHelper;
import com.courage.platform.schedule.console.core.trigger.XxlJobTrigger;
import com.courage.platform.schedule.console.core.util.I18nUtil;
import com.courage.platform.schedule.core.biz.model.ReturnT;
import com.courage.platform.schedule.core.biz.model.TriggerParam;
import com.courage.platform.schedule.core.enums.ExecutorBlockStrategyEnum;
import com.courage.platform.schedule.core.util.IpUtil;
import com.courage.platform.schedule.rpc.ScheduleRpcClient;
import com.courage.platform.schedule.rpc.protocol.TriggerScheduleCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

/**
 * Created by xuxueli on 17/3/10.
 */
public abstract class ExecutorRouter {

    protected static Logger logger = LoggerFactory.getLogger(ExecutorRouter.class);

    protected final static ScheduleRpcClient scheduleRpcClient = ScheduleRpcClient.getSingleInstance();

    private volatile static boolean init = false;

    public ExecutorRouter() {
        synchronized (ExecutorRouter.class) {
            if (!init) {
                logger.info("加载钩子……");
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    if (scheduleRpcClient != null) {
                        scheduleRpcClient.shutdown();
                    }
                }));
                init = true;
            }
        }
    }

    /**
     * 各个子类实现类，在该方法内部要实现重试机制
     *
     * @param jobInfo
     * @param group
     * @return
     */
    public abstract void routeRun(XxlJobInfo jobInfo, XxlJobGroup group);

    /**
     * 从数据库获取新的一条日志记录
     *
     * @param jobInfo
     * @return
     */
    protected XxlJobLog getJobLog(XxlJobInfo jobInfo) {
        XxlJobLog jobLog = new XxlJobLog();
        jobLog.setJobGroup(jobInfo.getJobGroup());
        jobLog.setJobId(jobInfo.getId());
        XxlJobDynamicScheduler.xxlJobLogDao.save(jobLog);
        logger.info(">>>>>>>>>>> hshc-schedule trigger start, jobLogId:{}", jobLog.getId());
        jobLog.setGlueType(jobInfo.getGlueType());
        jobLog.setExecutorHandler(jobInfo.getExecutorHandler());
        jobLog.setExecutorParam(jobInfo.getExecutorParam());
        jobLog.setTriggerTime(new Date());
        return jobLog;
    }

    /**
     * 构建触发器执行前的信息 日志记录
     *
     * @param jobInfo
     * @param group
     * @return
     */
    protected StringBuffer getTriggerMsg(XxlJobInfo jobInfo, XxlJobGroup group) {
        ExecutorBlockStrategyEnum blockStrategy = ExecutorBlockStrategyEnum.match(jobInfo.getExecutorBlockStrategy(), ExecutorBlockStrategyEnum.SERIAL_EXECUTION);  // block strategy
        ExecutorRouteStrategyEnum executorRouteStrategyEnum = ExecutorRouteStrategyEnum.match(jobInfo.getExecutorRouteStrategy(), null);    // route strategy
        ExecutorFailStrategyEnum failStrategy = ExecutorFailStrategyEnum.match(jobInfo.getExecutorFailStrategy(), ExecutorFailStrategyEnum.FAIL_ALARM);    // fail strategy
        StringBuffer triggerMsg = new StringBuffer();
        triggerMsg.append(I18nUtil.getString("jobconf_trigger_admin_adress")).append("：").append(IpUtil.getIp());
        triggerMsg.append("<br>").append(I18nUtil.getString("jobconf_trigger_exe_regtype")).append("：").append((group.getAddressType() == 0) ? I18nUtil.getString("jobgroup_field_addressType_0") : I18nUtil.getString("jobgroup_field_addressType_1"));
        triggerMsg.append("<br>").append(I18nUtil.getString("jobconf_trigger_exe_regaddress")).append("：").append(group.getRegistryList());
        triggerMsg.append("<br>").append(I18nUtil.getString("jobinfo_field_executorRouteStrategy")).append("：").append(executorRouteStrategyEnum.getTitle());
        triggerMsg.append("<br>").append(I18nUtil.getString("jobinfo_field_executorBlockStrategy")).append("：").append(blockStrategy.getTitle());
        triggerMsg.append("<br>").append(I18nUtil.getString("jobinfo_field_executorFailStrategy")).append("：").append(failStrategy.getTitle());
        return triggerMsg;
    }

    /**
     * 构建请求前参数
     *
     * @param jobInfo
     * @param jobLog
     * @return
     */
    protected TriggerParam getTriggerParam(XxlJobInfo jobInfo, XxlJobLog jobLog) {
        TriggerParam triggerParam = new TriggerParam();
        triggerParam.setJobId(jobInfo.getId());
        triggerParam.setExecutorHandler(jobInfo.getExecutorHandler());
        triggerParam.setExecutorParams(jobInfo.getExecutorParam());
        triggerParam.setExecutorBlockStrategy(jobInfo.getExecutorBlockStrategy());
        triggerParam.setLogId(jobLog.getId());
        triggerParam.setLogDateTim(jobLog.getTriggerTime().getTime());
        triggerParam.setGlueType(jobInfo.getGlueType());
        triggerParam.setGlueSource(jobInfo.getGlueSource());
        triggerParam.setGlueUpdatetime(jobInfo.getGlueUpdatetime().getTime());
        triggerParam.setBroadcastIndex(0);
        triggerParam.setBroadcastTotal(1);
        return triggerParam;
    }

    /**
     * 获取请求tcp的cmd对象
     *
     * @param triggerParam
     * @return
     */
    protected TriggerScheduleCommand getTcpCmd(TriggerParam triggerParam) {
        TriggerScheduleCommand command = new TriggerScheduleCommand();
        command.setServiceId(triggerParam.getExecutorHandler());
        command.setJobId(triggerParam.getJobId());
        command.setJobLogId(String.valueOf(triggerParam.getLogId()));
        command.setCreateMillisTime(System.currentTimeMillis());
        command.setExecutorParam(triggerParam.getExecutorParams());
        return command;
    }

    /**
     * 执行旧版的远程调用
     *
     * @param address
     * @param triggerParam
     */
    protected ReturnT<String> exec(String address, TriggerParam triggerParam) {
        // 执行远程服务
        return XxlJobTrigger.runExecutor(triggerParam, address);
    }

    /**
     * 执行完后记录触发结果
     *
     * @param jobLog
     * @param triggerResult
     * @param triggerMsg
     */
    protected void afterExec(XxlJobLog jobLog, ReturnT<String> triggerResult, StringBuffer triggerMsg, Boolean hasRetry) {
        StringBuffer runResultSB = new StringBuffer(I18nUtil.getString("jobconf_trigger_run") + "：");
        runResultSB.append("<br>address：").append(triggerResult.getContent());
        runResultSB.append("<br>code：").append(triggerResult.getCode());
        runResultSB.append("<br>msg：").append(triggerResult.getMsg());

        triggerResult.setMsg(runResultSB.toString());
        triggerResult.setContent(triggerResult.getContent());
        if (!hasRetry) {
            triggerMsg.append("<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>" + I18nUtil.getString("jobconf_trigger_run") + "<<<<<<<<<<< </span><br>").append(triggerResult.getMsg());
        } else {
            triggerMsg.append("<br><br><span style=\"color:#F39C12;\" > >>>>>>>>>>>" + I18nUtil.getString("jobconf_trigger_fail_retry") + "<<<<<<<<<<< </span><br>").append(triggerResult.getMsg());
        }

        jobLog.setExecutorAddress(triggerResult.getContent());
        jobLog.setTriggerCode(triggerResult.getCode());
        jobLog.setTriggerMsg(triggerMsg.toString());
        XxlJobDynamicScheduler.xxlJobLogDao.updateTriggerInfo(jobLog);
        // 6、monitor trigger
        JobFailMonitorHelper.monitor(jobLog.getId());
        logger.info(">>>>>>>>>>> hshc-schedule trigger end, jobLogId:{}", jobLog.getId());
    }

    protected String getLogAllInfo(Throwable e) {
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }
}
