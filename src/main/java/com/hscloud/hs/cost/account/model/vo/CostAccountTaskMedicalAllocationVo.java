package com.hscloud.hs.cost.account.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "核算结果医护分摊Vo")
public class CostAccountTaskMedicalAllocationVo {


    @Schema(description = "核算单元")
    private String accountUnit;

    @Schema(description = "核算单元核算值")
    private BigDecimal accountUnitValue;

    @Schema(description = "核算比例")
    private BigDecimal accountProportion;

    @Schema(description = "医护被分摊核算值")
    private BigDecimal medicalAllocationValue;
}
