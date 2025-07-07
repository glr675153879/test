package com.hscloud.hs.cost.account.model.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "步骤三：核算值Vo")
public class CalculatedValueVo {

    @Schema(description = "被分摊核算对象")
    private String accountObject;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "保留小数")
    private Integer reservedDecimal;

    @Schema(description = "进位规则")
    private String carryRule;

    @Schema(description = "借床分摊标识")
    private String bedAllocation;

    @Schema(description = "医护分摊标识")
    private String docNurseAllocation;

    @Schema(description = "医护分摊比例")
    private String allocate;

    @Schema(description = "核算项名称")
    private String configName;

    @Schema(description = "核算项描述")
    private String configDesc;

    @Schema(description = "核算值")
    private BigDecimal calculatedValue;
}
