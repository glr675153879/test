package com.hscloud.hs.cost.account.listener.kpi;

import org.springframework.context.ApplicationEvent;

/**
 * @author Admin
 */
public class KpiTaskCalculateEvent extends ApplicationEvent {


    public KpiTaskCalculateEvent(Object source) {
        super(source);
    }
}
