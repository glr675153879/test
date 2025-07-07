package com.hscloud.hs.cost.account.constant;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * imputation参数配置
 * 
 * @author hefeng
 */
@Configuration
@RefreshScope
@ConfigurationProperties(prefix = "imputation")
@Data
public class ImputaionProperties
{
    //行政组名
    private String xzGroupName = "行政组";

    //行政科室名
    private String xzUnitName = "行政科室";

    //鄞州门诊 科室单元
    private String yzmzUnitName = "鄞州门诊";
    private String yzmzGroupName = "医生组";

    //鄞州门诊收入归集人员
    private String yzmzIndexName = "鄞州门诊收入归集人员";

}
