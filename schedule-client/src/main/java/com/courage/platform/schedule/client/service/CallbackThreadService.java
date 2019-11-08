package com.courage.platform.schedule.client.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by 王鑫 on 2018/10/19.
 */
public class CallbackThreadService {

    private final static int POOL_CORE_SIZE = 5;

    private final static int POOL_MAX_SIZE = 15;

    private final static Logger logger = LoggerFactory.getLogger(CallbackThreadService.class);

    private CallbackThreadService() {
    }

    private static CallbackThreadService instance = new CallbackThreadService();

    private final static ThreadPoolExecutor callBackThread = new ThreadPoolExecutor(
            POOL_CORE_SIZE,
            POOL_MAX_SIZE,
            500,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(100000),
            new ThreadFactory() {
                private AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "CallBackThreadPool_" + this.threadIndex.incrementAndGet());
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    public static CallbackThreadService getSingleInstance() {
        return instance;
    }

    public ThreadPoolExecutor getCallBackThread() {
        return callBackThread;
    }

    public void close() {
        try {
            if (callBackThread != null) {
                callBackThread.shutdown();
            }
        } catch (Exception e) {
            logger.error("关闭scheduleRpcServer失败", e);
        }
    }
}
