package com.courage.platform.schedule.console.core.route.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.courage.platform.rpc.remoting.netty.protocol.PlatformRemotingCommand;
import com.courage.platform.rpc.remoting.netty.protocol.PlatformRemotingSysResponseCode;
import com.courage.platform.schedule.console.core.enums.ExecutorFailStrategyEnum;
import com.courage.platform.schedule.console.core.model.XxlJobGroup;
import com.courage.platform.schedule.console.core.model.XxlJobInfo;
import com.courage.platform.schedule.console.core.model.XxlJobLog;
import com.courage.platform.schedule.console.core.regcenter.RegistryController;
import com.courage.platform.schedule.console.core.route.ExecutorRouter;
import com.courage.platform.schedule.core.biz.model.ReturnT;
import com.courage.platform.schedule.core.biz.model.TriggerParam;
import com.courage.platform.schedule.rpc.protocol.CommandEnum;
import com.courage.platform.schedule.rpc.protocol.TriggerScheduleCommand;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

import static com.courage.platform.schedule.core.util.ExceptionUtil.splitString;

/**
 * Created by 王鑫 on 2018/11/5.
 */
public class ExecutorBroadcast extends ExecutorRouter {

    private static Logger logger = LoggerFactory.getLogger(ExecutorBroadcast.class);

    @Override
    public void routeRun(XxlJobInfo jobInfo, XxlJobGroup group) {
        try {
            //获取地址实例
            List<Instance> addressList = RegistryController.getInstanceList(group.getAppName());
            if (CollectionUtils.isNotEmpty(addressList)) {
                addressList.forEach(registryInstance -> {
                    XxlJobLog jobLog = null;
                    StringBuffer triggerMsg = null;
                    TriggerParam triggerParam = null;
                    //地址
                    String currentAddr = null;
                    //结果
                    ReturnT<String> triggerResult = null;
                    try {
                        //是否已经重试
                        Boolean hasRetry = false;
                        //获取日志基础信息
                        jobLog = getJobLog(jobInfo);
                        //设置触发信息
                        triggerMsg = getTriggerMsg(jobInfo, group);
                        //获取触发参数
                        triggerParam = getTriggerParam(jobInfo, jobLog);
                        //执行任务
                        Objects.requireNonNull(registryInstance, "从注册中心获取列表中元素存在null，addressList:" + addressList.toString());
                        if (StringUtils.isNotEmpty(registryInstance.getIp()) && registryInstance.getPort() > 0) {
                            currentAddr = registryInstance.getIp() + ":" + registryInstance.getPort();
                            triggerResult = execute(currentAddr, triggerParam);
                        }
                        // 失败重试
                        ExecutorFailStrategyEnum failStrategy = ExecutorFailStrategyEnum.match(jobInfo.getExecutorFailStrategy(), null);
                        if (triggerResult.getCode() != ReturnT.SUCCESS_CODE) {
                            if (failStrategy == ExecutorFailStrategyEnum.FAIL_RETRY) {
                                triggerResult = execute(currentAddr, triggerParam);
                                hasRetry = true;
                            }
                        }
                        //更新触发信息
                        afterExec(jobLog, triggerResult, triggerMsg, hasRetry);
                    } catch (Exception e) {
                        logger.error("触发广播任务调度失败，具体信息：jobInfo：" + JSON.toJSONString(jobInfo) + ",jobLog：" + JSON.toJSONString(jobLog) + ",triggerMsg：" + JSON.toJSONString(triggerMsg) + ",triggerParam：" + JSON.toJSONString(triggerParam) + ",currentAddr：" + currentAddr + ",triggerResult：" + JSON.toJSONString(triggerResult), e);
                    }
                });
            } else {
                throw new Exception("从注册中心拉取调用地址为null");
            }
        } catch (Exception e) {
            logger.error("触发广播任务调度失败，具体信息：jobInfo信息：" + JSON.toJSONString(jobInfo) + ",group信息：" + JSON.toJSONString(group), e);
        }
    }

    private ReturnT<String> execute(String address, TriggerParam triggerParam) {
        //流程变量定义
        ReturnT<String> triggerResult = new ReturnT<String>(null);
        //设置调用地址
        triggerResult.setContent(address);
        try {
            //获取请求tcp的cmd对象
            TriggerScheduleCommand command = getTcpCmd(triggerParam);
            PlatformRemotingCommand response = scheduleRpcClient.send(address, CommandEnum.TRIGGER_SCHEDULE_TASK_CMD, command);
            if (response != null && (response.getCode() == PlatformRemotingSysResponseCode.SUCCESS)) {
                triggerResult.setCode(ReturnT.SUCCESS_CODE);
                triggerResult.setMsg("触发广播调度成功，调用地址：" + address + "； 请求参数：" + JSON.toJSONString(command) + "； 响应结果：" + JSON.toJSONString(response));
                logger.info(triggerResult.getMsg());
            } else {
                throw new Exception("触发广播调度返回失败，调用地址：" + address + "； 请求参数：" + JSON.toJSONString(command) + "； 响应结果：" + JSON.toJSONString(response));
            }
        } catch (Throwable e) {
            String errorMsg = "Rpc exe error: serverAddress:" + address + "," + e.getMessage();
            logger.error(errorMsg, e);
            byte[] buff = getLogAllInfo(e).getBytes();
            int i = buff.length;
            if (i > 2048) {
                errorMsg = splitString(errorMsg, 2048);
            }
            triggerResult.setCode(ReturnT.FAIL_CODE);
            triggerResult.setMsg(errorMsg);
        }
        return triggerResult;
    }

}
