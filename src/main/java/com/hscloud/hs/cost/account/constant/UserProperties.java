package com.hscloud.hs.cost.account.constant;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * 人员考勤参数配置
 *
 * @author CJS
 */
@Configuration
@RefreshScope
@ConfigurationProperties(prefix = "user")
@Data
public class UserProperties
{
    private String url;

    private String appkey;

    private String appsecret;

    /**
     * 仅测试 虚拟数据使用，正式环境必须 false
     */
    private Boolean ifClosed = false;

    /**
     * 中台方法后缀，为了测试环境 和 演示环境区分
     */
    private String suffix = "";

}
