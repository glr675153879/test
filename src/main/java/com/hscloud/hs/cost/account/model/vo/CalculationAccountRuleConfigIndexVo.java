package com.hscloud.hs.cost.account.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "核算规则计算中核算指标Vo")
public class CalculationAccountRuleConfigIndexVo {

    @Schema(description = "配置项key")
    private String configKey;

    @Schema(description = "分摊规则id")
    private Long allocationRuleId;

    @Schema(description = "配置项指标id")
    private Long configIndexId;

    @Schema(description = "配置项指标名称")
    private String configIndexName;

}
