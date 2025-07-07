package com.hscloud.hs.cost.account.listener;

import org.springframework.context.ApplicationEvent;

/**
 * @author Admin
 */
public class TaskCalculateEvent extends ApplicationEvent {


    public TaskCalculateEvent(Object source) {
        super(source);
    }
}
