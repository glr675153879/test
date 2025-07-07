package com.hscloud.hs.cost.account.config;

import com.hscloud.hs.cost.account.service.impl.dataReport.CostReportRecordService;
import com.pig4cloud.pigx.common.job.core.ShedFactory;
import com.pig4cloud.pigx.common.job.core.ShedTaskInterface;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.support.KeepAliveLockProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Author:  Administrator
 * Date:  2024/9/20 10:01
 */
@Component
public class BaseConfig {
    private static Logger logger = LoggerFactory.getLogger(CostReportRecordService.class);

    private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    public final static String appCode = "cost";
    @Value("${kpi.period.month:1}")
    public Integer period_month;

    @Resource
    private Environment env;

    //@Autowired
    //private RemoteTaskService remoteTaskService;

    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        JdbcTemplateLockProvider jdbcTemplateLockProvider = new JdbcTemplateLockProvider(
                JdbcTemplateLockProvider.Configuration.builder()
                        .withJdbcTemplate(new JdbcTemplate(dataSource))
                        .withTableName("sys_shedlock")
                        .usingDbTime()
                        .build()
        );
        return new KeepAliveLockProvider(jdbcTemplateLockProvider, scheduler);
    }

    //@PostConstruct
//    public void InitTask() {
//        try {
//            boolean b = Boolean.parseBoolean(env.getProperty("open-task"));
//            ShedFactory.init(appCode, b, remoteTaskService);
//
//            ShedFactory.CreateTask(appCode + "_TestJob", "测试任务", 2L, 1L, "0 0/1 * * * ?",
//                    com.hscloud.hs.cost.account.job.TestJob.class.getName(),
//                    BaseConfig.appCode, 1L, 1L);
//
//            //RunTask(teskJob);
//        } catch (Exception ex) {
//            logger.error("任务初始化失败", ex);
//        }
//    }

    public static <T extends ShedTaskInterface> void RunTask(T r) {
        RunTask(r, null);
    }

    public static <T extends ShedTaskInterface> void RunTask(T r, String param) {
        new Thread(() -> {
            try {
                r.action(param);
            } catch (Exception e) {
                logger.error("任务执行异常", e);
            }
        }).start();
    }
}
