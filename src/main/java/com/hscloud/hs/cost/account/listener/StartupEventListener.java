package com.hscloud.hs.cost.account.listener;

import com.pig4cloud.pigx.common.data.monitor.listener.event.DBChangeEvent;
import com.pig4cloud.pigx.common.data.monitor.listener.event.EventContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class StartupEventListener {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    //@EventListener(ContextRefreshedEvent.class)
    @PostConstruct
    public void onApplicationEvent() {
        EventContent eventContent = new EventContent();
        eventContent.setNeedSendMessage(false);
        eventContent.setSuffix(":cost");
        applicationEventPublisher.publishEvent(new DBChangeEvent(eventContent));
    }
}