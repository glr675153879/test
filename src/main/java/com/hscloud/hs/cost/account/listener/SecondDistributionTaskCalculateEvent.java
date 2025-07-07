package com.hscloud.hs.cost.account.listener;

import org.springframework.context.ApplicationEvent;

/**
 * 二次分配任务监听
 */
public class SecondDistributionTaskCalculateEvent extends ApplicationEvent {


    public SecondDistributionTaskCalculateEvent(Object source) {
        super(source);
    }
}
