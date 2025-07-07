package com.hscloud.hs.cost.account.model.vo.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class KpiItemCoefficientVO {
    @Schema(description = "科室id")
    private Long accountUnitId;

    @Schema(description = "人员id")
    private Long userId;

    @Schema(description = "人员姓名")
    private String empName;

    @Schema(description = "系数")
    private BigDecimal coefficient;
}
