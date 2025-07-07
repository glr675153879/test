package com.hscloud.hs.cost.account.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class ThreadPoolConfig {
 
    @Bean(name = "threadPoolExecutor")
    public Executor threadPoolExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //核心线程数
        executor.setCorePoolSize(10);
        //最大线程数
        executor.setMaxPoolSize(50);
        //工作队列
        executor.setQueueCapacity(100);
        //空闲线程存活时间
        executor.setKeepAliveSeconds(60);
        //线程名称前缀
        executor.setThreadNamePrefix("Custom-Executor-");
        //拒绝策略
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        // 必须初始化
        executor.initialize();
        return executor;
    }
}