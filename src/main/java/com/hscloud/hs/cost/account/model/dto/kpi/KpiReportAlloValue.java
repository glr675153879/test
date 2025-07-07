package com.hscloud.hs.cost.account.model.dto.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class KpiReportAlloValue {
    @Schema(description = "指标/核算项名称")
    private String name;
    @Schema(description = "指标/核算项code")
    private String code;
    @Schema(description = "类型 1核算项2指标")
    private String type;
    private Long userId;
    private String userName;
    private Long deptId;
    private String deptName;
    private Long imputationDeptId;
    private String imputationDeptName;
    private String sourceDept;
    @Schema(description = "值")
    private BigDecimal value;
}
