package com.hscloud.hs.cost.account.constant;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * 科室成本参数配置
 * 
 * @author hefeng
 */
@Configuration
@RefreshScope
@ConfigurationProperties(prefix = "deptcost")
@Data
public class DeptCostProperties
{
    //首次任务周期
    private String initCycle = "202408";


}
