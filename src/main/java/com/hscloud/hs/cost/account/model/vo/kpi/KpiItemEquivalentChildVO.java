package com.hscloud.hs.cost.account.model.vo.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class KpiItemEquivalentChildVO {
    private Long id;

    @Schema(description = "人员id")
    private Long userId;

    @Schema(description = "员工姓名")
    private String empName;

    @Schema(description = "当量类型 1-人 2-科室")
    private String equivalentType;

    @Schema(description = "总工作量（原始值）")
    private BigDecimal totalWorkload;

    @Schema(description = "总工作量（管理员校准值）")
    private BigDecimal totalWorkloadAdmin;

    @Schema(description = "总工作量（新值）")
    private BigDecimal newTotalWorkload;

    @Schema(description = "总工作当量")
    private BigDecimal totalEquivalent;

    @Schema(description = "系数")
    private BigDecimal coefficient;
}
