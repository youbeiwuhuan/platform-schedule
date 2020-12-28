package com.courage.platform.schedule.raft.test.counter;

/**
 * Created by zhangyong on 2020/12/9.
 */
public class CounterServer {

    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Useage : java com.alipay.sofa.jraft.example.counter.CounterServer {dataPath} {groupId} {serverId} {initConf}");
            System.out.println("Example: java com.alipay.sofa.jraft.example.counter.CounterServer /tmp/server1 counter 127.0.0.1:8081 127.0.0.1:8081,127.0.0.1:8082,127.0.0.1:8083");
            System.exit(1);
        }
        final String dataPath = args[0];
        final String groupId = args[1];
        final String serverIdStr = args[2];
        final String initConfStr = args[3];
        

    }

}
