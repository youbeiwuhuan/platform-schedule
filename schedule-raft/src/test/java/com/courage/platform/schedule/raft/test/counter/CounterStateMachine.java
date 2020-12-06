package com.courage.platform.schedule.raft.test.counter;

import com.alipay.sofa.jraft.Iterator;
import com.alipay.sofa.jraft.core.StateMachineAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

public class CounterStateMachine extends StateMachineAdapter {

    private static final Logger logger = LoggerFactory.getLogger(CounterStateMachine.class);

    /**
     * Counter value
     */
    private final AtomicLong value = new AtomicLong(0);

    @Override
    public void onApply(Iterator iterator) {

    }

}
