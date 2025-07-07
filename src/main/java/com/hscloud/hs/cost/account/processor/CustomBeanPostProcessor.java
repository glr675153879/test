package com.hscloud.hs.cost.account.processor;

import com.hscloud.hs.cost.account.handler.CostSqlInjector;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class CustomBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        if ("dataScopeSqlInjector".equals(beanName)) {
            return new CostSqlInjector();
        }
        return bean;
    }
}