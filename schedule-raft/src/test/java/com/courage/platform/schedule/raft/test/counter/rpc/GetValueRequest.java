package com.courage.platform.schedule.raft.test.counter.rpc;

import java.io.Serializable;

/**
 * Created by zhangyong on 2020/12/8.
 */
public class GetValueRequest implements Serializable {

    private static final long serialVersionUID = 9218253805003988802L;

    private boolean           readOnlySafe     = true;

    public boolean isReadOnlySafe() {
        return readOnlySafe;
    }

    public void setReadOnlySafe(boolean readOnlySafe) {
        this.readOnlySafe = readOnlySafe;
    }

}
