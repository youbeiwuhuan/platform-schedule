package com.courage.platform.schedule.core.util;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 1位 + 41bit(时间戳) + 10bit(机器id) + 12bit(序号) 最多一毫秒有4095个自增序列id
 * 模仿twitter的snowflake的做法
 * Created by zhangyong on 2018/10/13.
 */
public class IdGenerator {

    //总的分区数
    private final static int SHARDING_NUM = ShardingConstants.SHARDING_LENGTH;

    //最大机器id 1023 也就是 1111111111
    private final static int MAX_WORKER_ID = SHARDING_NUM - 1;

    //最大序号
    private final static int MAX_SEQ = 4095;

    private final static long WORKERID_BITS = 10L;

    private final static long SEQUENCE_BITS = 12L;

    private final static long TIMESTAMP_LEFTSHIFT = SEQUENCE_BITS + WORKERID_BITS;

    private final static long WORKERID_SHIFT = SEQUENCE_BITS;

    private static AtomicInteger currentSeq = new AtomicInteger(0);

    private static Object lock = new Object();

    public static long getUniqueIdAutoSeq(int workerId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException("workerId is not Illegal");
        }
        int myseq = currentSeq.incrementAndGet();
        if (myseq >= MAX_SEQ) {
            synchronized (lock) {
                currentSeq.set(0);
                myseq = currentSeq.incrementAndGet();
            }
        }
        //时间戳
        long timestamp = System.currentTimeMillis();
        //机器编号
        return (timestamp - 1288834974657L << TIMESTAMP_LEFTSHIFT) | (workerId << WORKERID_SHIFT) | myseq;
    }


    public static long getUniqueId(int workerId, int seqId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException("workerId is not Illegal");
        }
        if (seqId > MAX_SEQ || seqId < 0) {
            throw new IllegalArgumentException("seqId is not Illegal ");
        }
        //时间戳
        long timestamp = System.currentTimeMillis();
        //机器编号
        return (timestamp - 1288834974657L << TIMESTAMP_LEFTSHIFT) | (workerId << WORKERID_SHIFT) | seqId;
    }

    public static Integer getWorkerId(Long uniqueId) {
        Long workerId = (uniqueId >> 12) & 0x03ff;
        return workerId.intValue();
    }

    public static void main(String[] args) {
        String a = "46";
        int workerId = Math.abs(a.hashCode()) % 1024;
        System.out.println(Integer.toBinaryString(workerId));
        System.out.println(workerId);
        long uniqueId = IdGenerator.getUniqueId(workerId, 1);
        System.out.println(uniqueId);
        //通过方法反算出 workerId 信息
        Integer reverseWorkerId = getWorkerId(uniqueId);
        System.out.println(reverseWorkerId);

        for (int i = 0; i < 10; i++) {
            String uuid = UUID.randomUUID().toString();
            int workerId2 = Math.abs(uuid.hashCode()) % 1024;
            Long seqId = IdGenerator.getUniqueId(workerId2, 0);
            System.out.println(seqId);
        }

    }

}
