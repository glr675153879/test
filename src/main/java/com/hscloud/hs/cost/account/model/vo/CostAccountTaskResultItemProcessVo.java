package com.hscloud.hs.cost.account.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(description = "核算项结果Vo")
public class CostAccountTaskResultItemProcessVo {

    @Schema(description = "步骤一：被分摊对象核算值计算")
    private AllocationObjectCalculationVo stepOne;

    @Schema(description = "步骤二：核算规则计算")
    private CalculationAccountRuleVo stepTwo;

    @Schema(description = "步骤三：核算值")
    private CalculatedValueVo stepThree;
}
