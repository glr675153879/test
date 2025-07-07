package com.hscloud.hs.cost.account.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;


@Data
@Schema(description = "返回核算项vo")
public class CostIndexConfigItemVo implements Serializable {
    @Schema(description = "核算指标核算项配置的主键id")
    private Long id;

    @Schema(description = "指标项id")
    private Long indexId;

    @Schema(description = "配置项id")
    private Long configId;

    @Schema(description = "配置项key")
    private String configKey;

    @Schema(description ="配置项名称")
    private String configName;

    @Schema(description ="配置项描述")
    private String configDesc;

    @Schema(description = "计算维度")
    private String dimension;

    @Schema(description = "核算对象")
    private String accountObject;

    @Schema(description = "核算范围")
    private String accountRange;

    @Schema(description = "核算集")
    private String accounts;

    @Schema(description = "核算单元性质")
    private String accountDeptShip;

    @Schema(description = "核算周期 current:当前周期 before:上一周期")
    private String accountPeriod;

    @Schema(description = "计量单位")
    private String measureUnit;

    @Schema(description = "保留小数位数")
    private Integer retainDecimal;

    @Schema(description = "进位规则")
    private String carryRule;


}
