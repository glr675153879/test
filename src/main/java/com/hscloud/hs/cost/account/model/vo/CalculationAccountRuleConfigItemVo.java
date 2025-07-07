package com.hscloud.hs.cost.account.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "核算规则计算中核算项Vo")
public class CalculationAccountRuleConfigItemVo {

    @Schema(description = "分摊规则id")
    private Long allocationRuleId;

    @Schema(description = "配置项key")
    private String configKey;

    @Schema(description = "配置项id")
    private Long configId;

    @Schema(description = "配置项名称")
    private String configName;

    @Schema(description = "计算维度")
    private String dimension;

    @Schema(description = "配置项描述")
    private String configDesc;

    @Schema(description = "核算比例")
    private String accountProportion;

    @Schema(description = "核算比例名称")
    private String accountProportionDesc;

    @Schema(description = "比例通用id")
    private Long proportionBaseId;

    @Schema(description = "核算比例id")
    private Long accountProportionId;

}
