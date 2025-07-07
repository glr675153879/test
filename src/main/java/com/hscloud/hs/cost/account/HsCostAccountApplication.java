package com.hscloud.hs.cost.account;

import com.pig4cloud.pigx.common.data.monitor.anno.EnableDataMonitor;
import com.pig4cloud.pigx.common.feign.annotation.EnablePigxFeignClients;
import com.pig4cloud.pigx.common.job.annotation.EnablePigxXxlJob;
import com.pig4cloud.pigx.common.security.annotation.EnablePigxResourceServer;
import com.pig4cloud.pigx.common.swagger.annotation.EnableOpenApi;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author Admin
 */
@EnableOpenApi("cost")
@EnablePigxXxlJob
@EnablePigxResourceServer
@EnableDiscoveryClient
@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableDataMonitor
@EnablePigxFeignClients(basePackages = {"com.hscloud.hs","com.pig4cloud.pigx"})
@EnableSchedulerLock(defaultLockAtMostFor = "1m", defaultLockAtLeastFor = "0s")
//@EnableAsync
@MapperScan({"com.hscloud.hs.cost.account.mapper", "com.gitee.sunchenbin.mybatis.actable.dao.*"})
@ComponentScan(basePackages = {"com.hscloud.hs.cost.account.*", "com.gitee.sunchenbin.mybatis.actable.manager.*"})

public class HsCostAccountApplication {

    public static void main(String[] args) {
        SpringApplication.run(HsCostAccountApplication.class, args);
    }

}
