package com.hscloud.hs.cost.account.model.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SecondUserDistributionUnitSingle {

    @Schema(description = "average平均绩效,individualPost个人岗位绩效")
    private String type;

    @Schema(description = "核算指标id")
    private Long indexId;

    @Schema(description = "核算指标名称")
    private String indexName;

    @Schema(description = "核算方案id")
    private Long planId;

    @Schema(description = "金额")
    private BigDecimal amount;


    @Schema(description = "职称级别")
    private String titleLevel;


    @Schema(description = "学历")
    private String education;


    @Schema(description = "个人岗位系数")
    private BigDecimal coefficient;
}
