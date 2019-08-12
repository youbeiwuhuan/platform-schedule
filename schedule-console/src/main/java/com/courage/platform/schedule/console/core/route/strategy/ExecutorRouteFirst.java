package com.courage.platform.schedule.console.core.route.strategy;

import com.courage.platform.schedule.console.core.enums.ExecutorFailStrategyEnum;
import com.courage.platform.schedule.console.core.model.XxlJobGroup;
import com.courage.platform.schedule.console.core.model.XxlJobInfo;
import com.courage.platform.schedule.console.core.model.XxlJobLog;
import com.courage.platform.schedule.console.core.route.ExecutorRouter;
import com.courage.platform.schedule.core.biz.model.ReturnT;
import com.courage.platform.schedule.core.biz.model.TriggerParam;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 王鑫 on 2018/11/5.
 */
public class ExecutorRouteFirst extends ExecutorRouter {

    @Override
    public void routeRun(XxlJobInfo jobInfo, XxlJobGroup group) {
        XxlJobLog jobLog = null;
        StringBuffer triggerMsg = null;
        TriggerParam triggerParam = null;
        try {
            //是否已经重试
            Boolean hasRetry = false;
            //获取日志基础信息
            jobLog = getJobLog(jobInfo);
            //设置触发信息
            triggerMsg = getTriggerMsg(jobInfo, group);
            //获取触发参数
            triggerParam = getTriggerParam(jobInfo, jobLog);
            //获取地址实例
            List<RegistryInstance> addressList = getAddress(group);
            //执行任务
            ReturnT<String> triggerResult = execute(group, addressList, triggerMsg, triggerParam);
            // 失败重试
            ExecutorFailStrategyEnum failStrategy = ExecutorFailStrategyEnum.match(jobInfo.getExecutorFailStrategy(), null);
            if (triggerResult.getCode() != ReturnT.SUCCESS_CODE) {
                if (failStrategy == ExecutorFailStrategyEnum.FAIL_RETRY) {
                    triggerResult = execute(group, addressList, triggerMsg, triggerParam);
                    hasRetry = true;
                }
            }
            //更新触发信息
            afterExec(jobLog, triggerResult, triggerMsg, hasRetry);
        } catch (Throwable e) {
            logger.error("RouteFirst任务调度失败，具体信息：jobInfo：" + JSON.toJSONString(jobInfo) + ",jobLog：" + JSON.toJSONString(jobLog) + ",triggerMsg：" + JSON.toJSONString(triggerMsg) + ",triggerParam：" + JSON.toJSONString(triggerParam), e);
        }
    }

    private ReturnT<String> execute(XxlJobGroup group, List<RegistryInstance> addressList, StringBuffer triggerMsg, TriggerParam triggerParam) {
        //流程变量定义
        ReturnT<String> triggerResult = new ReturnT<String>(null);
        List<String> addrList = null;
        try {
            if (CollectionUtils.isNotEmpty(addressList)) {
                //tcp
                triggerResult.setServiceGroupEnum(ServiceGroupEnum.TASKRPC);
                //获取请求tcp的cmd对象
                TriggerScheduleCommand command = getTcpCmd(triggerParam);
                addrList = new ArrayList<String>();
                for (RegistryInstance registryInstance : addressList) {
                    try {
                        if (StringUtils.isNotEmpty(registryInstance.getIp()) && registryInstance.getPort() > 0) {
                            String currentAddr = registryInstance.getIp() + ":" + registryInstance.getPort();
                            addrList.add(currentAddr);
                            PlatformRemotingCommand response = scheduleRpcClient.send(currentAddr, CommandEnum.TRIGGER_SCHEDULE_TASK_CMD, command);
                            if (response != null && (response.getCode() == PlatformRemotingSysResponseCode.SUCCESS)) {
                                triggerResult.setCode(ReturnT.SUCCESS_CODE);
                                //设置调用地址
                                triggerResult.setContent(currentAddr);
                                triggerResult.setMsg("触发成功，调用地址：" + currentAddr + "； 请求参数：" + JSON.toJSONString(command) + "； 响应结果：" + JSON.toJSONString(response));
                                logger.info(triggerResult.getMsg());
                                break;
                            } else {
                                throw new Exception("触发返回失败，调用地址：" + currentAddr + "； 请求参数：" + JSON.toJSONString(command) + "； 响应结果：" + JSON.toJSONString(response));
                            }
                        }
                    } catch (Throwable e) {
                        String error = "调度任务失败,请求命令信息：" + JSON.toJSONString(command) + "  ； " + e.getMessage();
                        triggerResult.setCode(ReturnT.FAIL_CODE);
                        triggerResult.setMsg(error + getLogAllInfo(e));
                        logger.error(error, e);
                    }
                }
                if (triggerResult.getCode() == ReturnT.FAIL_CODE) {
                    triggerResult.setContent(StringUtils.join(addrList, ','));
                }
            } else {
                triggerResult.setCode(ReturnT.FAIL_CODE);
                triggerMsg.append("<br>----------------------<br>").append("TASKRPC" + I18nUtil.getString("jobconf_trigger_address_empty"));
            }

            //执行旧远程服务
            if (triggerResult == null || triggerResult.getCode() == ReturnT.FAIL_CODE) {
                List<String> oldAddr = group.getRegistryList();
                if (CollectionUtils.isNotEmpty(oldAddr)) {
                    //将之前的结果放进去
                    triggerParam.setReturnT(triggerResult);
                    //执行
                    triggerResult = exec(oldAddr.get(0), triggerParam);
                } else {
                    triggerMsg.append("<br>----------------------<br>").append("HTTPRPC" + I18nUtil.getString("jobconf_trigger_address_empty"));
                }
            }
        } catch (Exception e) {
            String errorMsg = "Rpc exe error: serverAddress:" + addressList.toString() + "," + e.getMessage();
            logger.error(errorMsg, e);
            byte[] buff = getStackTrace(e).getBytes();
            int i = buff.length;
            if (i > 2048) {
                errorMsg = splitString(errorMsg, 2048);
            }
            triggerResult.setCode(ReturnT.FAIL_CODE);
            triggerResult.setMsg(errorMsg);
            triggerResult.setContent(addrList.toString());
        }
        return triggerResult;
    }
}
