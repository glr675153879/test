package com.hscloud.hs.cost.account.validator;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Admin
 */
@Component
public class ValidatorHolder implements ApplicationContextAware {

    private final Map<String,BaseValidator> baseValidatorMap = new ConcurrentHashMap<>(8);

    @Autowired
    private ConfigurableListableBeanFactory beanFactory;

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) {
        String[] beanNames = beanFactory.getBeanNamesForType(BaseValidator.class);
        for (String beanName : beanNames) {
            BaseValidator baseValidator = (BaseValidator) beanFactory.getBean(beanName);
            baseValidatorMap.put(baseValidator.getType(), baseValidator);
        }
    }

    public  BaseValidator getValidatorByType(String type) {
        return baseValidatorMap.get(type);
    }
}

