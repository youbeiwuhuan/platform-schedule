package com.courage.platform.schedule.raft.test.counter.rpc;

import com.alipay.sofa.jraft.rpc.RpcContext;
import com.alipay.sofa.jraft.rpc.RpcProcessor;

import java.io.Serializable;

/**
 * Created by zhangyong on 2020/12/8.
 */
public class GetValueRequestProcessor implements RpcProcessor<GetValueRequest> {

    @Override
    public void handleRequest(RpcContext rpcContext, GetValueRequest getValueRequest) {

    }

    @Override
    public String interest() {
        return null;
    }

}
