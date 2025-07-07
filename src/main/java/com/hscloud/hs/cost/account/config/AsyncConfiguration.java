package com.hscloud.hs.cost.account.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfiguration {

    // 为基础平台的 AsyncConfiguration 提供的线程池
    @Bean("applicationTaskExecutor")
    public ThreadPoolTaskExecutor applicationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(200);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("secondAsync-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean("secondAsync")
    public Executor secondAsync() {
        //异步线程中传递上下文
        return new DelegatingSecurityContextAsyncTaskExecutor(applicationTaskExecutor());
    }

    @Bean("dcAsync")
    public Executor dcAsync() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = applicationTaskExecutor();
        threadPoolTaskExecutor.setThreadNamePrefix("dcAsync-");
        //异步线程中传递上下文
        return new DelegatingSecurityContextAsyncTaskExecutor(threadPoolTaskExecutor);
    }

}