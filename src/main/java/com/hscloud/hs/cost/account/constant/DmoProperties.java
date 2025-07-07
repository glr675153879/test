package com.hscloud.hs.cost.account.constant;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * fdfs参数配置
 *
 * @author htywzj
 */
@Configuration
@RefreshScope
@ConfigurationProperties(prefix = "dmo")
@Data
public class DmoProperties
{
    private String url;

    private String appkey;

    private String appsecret;

    /**
     * 仅测试 虚拟数据使用，正式环境必须 false
     */
    private Boolean ifClosed = false;

    /**
     * 基础科室 病区存入redis  (科室接口响应太慢6s)
     */
    private Boolean ifRedis = false;

    /**
     * 检查单有影像材料的大类
     */
    private String examClass = "";
    /**
     * 是否要从结果集 过滤 住院 和出院
     */
    private Boolean isInhos = false;

    /**
     * 中台方法后缀，为了测试环境 和 演示环境区分
     */
    private String suffix = "";

    /**
     * 是否部署危机
     */
    private Boolean isDangerous = false;

	/**
	 * 是否部署移动查房
	 */
	private Boolean ifWard = false;

	/**
	 * 是否部署门诊助手
	 */
	private Boolean ifOutpatient = false;

    /**
     * 2024年9月及之前的走数据小组接口
     */
    private String apiMonth = "2024-09";

}
